package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.utils.NamedThreadFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author noear
 * @since 1.0
 */
public class MqServerImpl extends BuilderListener implements MqServer {
    private Server server;
    private ExecutorService distributeExecutor;

    private Map<String, Set<String>> subscribeMap = new HashMap<>();
    private Map<String, MqUserQueue> userMap = new HashMap<>();
    private Map<String, String> accessMap = new HashMap<>();


    @Override
    public MqServer addAccess(String accessKey, String accessSecretKey) {
        accessMap.put(accessKey, accessSecretKey);
        return this;
    }

    @Override
    public MqServer distributeExecutor(ExecutorService distributeExecutor) {
        if (distributeExecutor != null) {
            this.distributeExecutor = distributeExecutor;
        }

        return this;
    }

    private void initDistributeExecutor() {
        if (distributeExecutor == null) {
            int distributePoolSize = Runtime.getRuntime().availableProcessors() * 2;
            distributeExecutor = new ThreadPoolExecutor(distributePoolSize, distributePoolSize,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue(),
                    new NamedThreadFactory("FolkMQ-distributeExecutor-"));
        }
    }

    @Override
    public MqServer stop() {
        server.stop();
        return this;
    }

    @Override
    public MqServer start(int port) throws Exception {
        //初始化派发执行器
        initDistributeExecutor();

        //启动服务
        server = SocketD.createServer("sd:tcp")
                .config(c -> c.port(port))
                .listen(this)
                .start();

        //接收订阅指令
        on(MqConstants.MQ_CMD_SUBSCRIBE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                //表示我收到了
                s.replyEnd(m, new StringEntity(""));
            }

            String topic = m.meta(MqConstants.MQ_TOPIC);
            String user = m.meta(MqConstants.MQ_USER);

            onSubscribe(topic, user, s);
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

                distribute(topic, scheduled, m);
            });
        });

        return this;
    }

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
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);

        //遍历这个会话身上的身份记录（有些可能不是）
        for (String user : session.attrMap().keySet()) {
            MqUserQueue messageQueue = userMap.get(user);

            //如果找到对应的队列
            if (messageQueue != null) {
                messageQueue.removeSession(session);
            }
        }
    }

    /**
     * 当订阅时
     */
    private synchronized void onSubscribe(String topic, String user, Session session) {
        //给会话添加身份（可以有多个不同的身份）
        session.attr(user, "1");

        //以身份进行订阅(topic -> user)
        Set<String> userSet = subscribeMap.get(topic);
        if (userSet == null) {
            userSet = new HashSet<>();
            subscribeMap.put(topic, userSet);
        }

        userSet.add(user);

        //为身份建立队列(user -> queue)
        if (userMap.containsKey(user) == false) {
            MqUserQueue messageQueue = new MqUserQueueImpl(user);
            messageQueue.addSession(session);
            userMap.put(user, messageQueue);
        }
    }

    /**
     * 当发布时
     */
    private void distribute(String topic, long scheduled, Message message) {
        //取出所有订阅的身份
        Set<String> userSet = subscribeMap.get(topic);
        if (userSet != null) {
            for (String user : userSet) {
                MqUserQueue queue = userMap.get(user);
                if (queue != null) {
                    MqMessageHolder messageHolder = new MqMessageHolder(message, scheduled);
                    queue.push(messageHolder);
                }
            }
        }
    }
}