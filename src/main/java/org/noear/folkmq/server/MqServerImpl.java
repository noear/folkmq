package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 消息服务端
 *
 * @author noear
 * @since 1.0
 */
public class MqServerImpl extends BuilderListener implements MqServer {
    private static final Logger log = LoggerFactory.getLogger(MqServerImpl.class);

    private Server server;
    private ExecutorService distributeExecutor;

    private Map<String, Set<String>> subscribeMap = new HashMap<>();
    private Map<String, MqConsumerQueue> consumerMap = new HashMap<>();
    private Map<String, String> accessMap = new HashMap<>();


    public MqServerImpl() {
        //::初始化 BuilderListener(self) 的路由监听

        //接收订阅指令
        on(MqConstants.MQ_CMD_SUBSCRIBE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                //表示我收到了
                s.replyEnd(m, new StringEntity(""));
            }

            String topic = m.meta(MqConstants.MQ_TOPIC);
            String consumer = m.meta(MqConstants.MQ_CONSUMER);

            onSubscribe(topic, consumer, s);
        });

        //接收发布指令
        on(MqConstants.MQ_CMD_PUBLISH, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                //表示我收到了
                s.replyEnd(m, new StringEntity(""));
            }

            distributeExecutor.submit(() -> {
                String topic = m.meta(MqConstants.MQ_TOPIC);
                long scheduled = Long.parseLong(m.metaOrDefault(MqConstants.MQ_SCHEDULED, "0"));

                distributeDo(topic, scheduled, m);
            });
        });
    }

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    @Override
    public MqServer addAccess(String accessKey, String accessSecretKey) {
        accessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置派发执行器
     *
     * @param distributeExecutor 线程池
     */
    @Override
    public MqServer distributeExecutor(ExecutorService distributeExecutor) {
        if (distributeExecutor != null) {
            this.distributeExecutor = distributeExecutor;
        }

        return this;
    }

    /**
     * 初始化派发执行器
     */
    private void initDistributeExecutor() {
        if (distributeExecutor == null) {
            int distributePoolSize = Runtime.getRuntime().availableProcessors() * 2;
            distributeExecutor = new ThreadPoolExecutor(distributePoolSize, distributePoolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue(),
                    new NamedThreadFactory("FolkMQ-distributeExecutor-"));
        }
    }

    /**
     * 启动
     */
    @Override
    public MqServer start(int port) throws Exception {
        //初始化派发执行器
        initDistributeExecutor();

        //启动 SocketD 服务（使用 tpc 通讯）
        server = SocketD.createServer("sd:tcp")
                .config(c -> c.port(port))
                .listen(this)
                .start();
        return this;
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        server.stop();
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        if (accessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                session.close();
                return;
            }

            if (accessSecretKey.equals(accessMap.get(accessKey)) == false) {
                session.close();
                return;
            }
        }

        log.info("Channel session opened, session={}", session.sessionId());
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);

        log.info("Channel session closed, session={}", session.sessionId());

        //遍历这个会话身上的身份记录（有些可能不是）
        for (String consumer : session.attrMap().keySet()) {
            MqConsumerQueue messageQueue = consumerMap.get(consumer);

            //如果找到对应的队列
            if (messageQueue != null) {
                messageQueue.removeSession(session);
            }
        }
    }

    /**
     * 会话出错时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        log.error("Channel error, session={}", session.sessionId(), error);
    }

    /**
     * 当订阅时
     */
    private synchronized void onSubscribe(String topic, String consumer, Session session) {
        log.info("Channel subscribe topic={}, consumer={}, session={}", topic, consumer, session.sessionId());

        //给会话添加身份（可以有多个不同的身份）
        session.attr(consumer, "1");

        //以身份进行订阅(topic -> consumer)
        Set<String> consumerSet = subscribeMap.get(topic);
        if (consumerSet == null) {
            consumerSet = new HashSet<>();
            subscribeMap.put(topic, consumerSet);
        }

        consumerSet.add(consumer);

        //为身份建立队列(consumer -> queue)
        MqConsumerQueue consumerQueue = consumerMap.get(consumer);
        if (consumerQueue == null) {
            consumerQueue = new MqConsumerQueueImpl(consumer);
            consumerMap.put(consumer, consumerQueue);
        }

        consumerQueue.addSession(session);
    }

    /**
     * 派发执行
     *
     * @param topic     主题
     * @param scheduled 预定派发时间
     * @param message   消息源
     */
    private void distributeDo(String topic, long scheduled, Message message) {
        //取出所有订阅的身份
        Set<String> consumerSet = subscribeMap.get(topic);
        if (consumerSet != null) {
            for (String consumer : consumerSet) {
                MqConsumerQueue queue = consumerMap.get(consumer);
                if (queue != null) {
                    MqMessageHolder messageHolder = new MqMessageHolder(message, scheduled);
                    queue.push(messageHolder);
                }
            }
        }
    }
}