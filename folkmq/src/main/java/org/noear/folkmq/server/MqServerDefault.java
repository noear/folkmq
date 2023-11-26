package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.transport.server.ServerConfigHandler;
import org.noear.socketd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 消息服务端默认实现
 *
 * @author noear
 * @since 1.0
 */
public class MqServerDefault extends EventListener implements MqServerInternal {
    private static final Logger log = LoggerFactory.getLogger(MqServerDefault.class);

    //服务端
    private Server server;
    //服务端访问账号
    private Map<String, String> serverAccessMap = new HashMap<>();
    //服务端配置处理
    private ServerConfigHandler serverConfigHandler;

    //持久化接口
    private MqPersistent persistent;

    //订阅关系表(topicConsumer=>MqTopicConsumerQueue)
    private Map<String, Set<String>> subscribeMap = new HashMap<>();
    //主题消费队列表(topicConsumer=>MqTopicConsumerQueue)
    private Map<String, MqTopicConsumerQueue> topicConsumerMap = new HashMap<>();


    public MqServerDefault() {
        //::初始化 Persistent 接口

        persistent = new MqPersistentDefault();
        persistent.init(this);

        //::初始化 BuilderListener(self) 的路由监听

        //接收订阅指令
        on(MqConstants.MQ_EVENT_SUBSCRIBE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumer = m.meta(MqConstants.MQ_META_CONSUMER);

            //持久化::订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            persistent.onSubscribe(topic, consumer, s);

            //持久化后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) {
                //发送“确认”，表示服务端收到了
                s.replyEnd(m, new StringEntity(""));
            }

            //执行订阅
            subscribeDo(topic, consumer, s);
        });

        //接收取消订阅指令
        on(MqConstants.MQ_EVENT_UNSUBSCRIBE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumer = m.meta(MqConstants.MQ_META_CONSUMER);

            //持久化::取消订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            persistent.onUnSubscribe(topic, consumer, s);

            //持久化后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) {
                //发送“确认”，表示服务端收到了
                s.replyEnd(m, new StringEntity(""));
            }

            //执行取消订阅
            unsubscribeDo(topic, consumer, s);
        });

        //接收发布指令
        on(MqConstants.MQ_EVENT_PUBLISH, (s, m) -> {
            //执行派发
            String topic = m.meta(MqConstants.MQ_META_TOPIC);

            //持久化::发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            persistent.onPublish(topic, m);

            //持久化后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) { //此判断兼容 Qos0, Qos1
                //发送“确认”，表示服务端收到了
                s.replyEnd(m, new StringEntity(""));
            }

            //执行交换
            exchangeDo(topic, m);
        });

        //接收保存指令
        on(MqConstants.MQ_EVENT_SAVE, (s, m) -> {
            save();
        });
    }

    /**
     * 服务端配置
     */
    @Override
    public MqServer config(ServerConfigHandler configHandler) {
        serverConfigHandler = configHandler;
        return this;
    }

    @Override
    public MqServer persistent(MqPersistent persistent) {
        if (persistent != null) {
            this.persistent = persistent;
            this.persistent.init(this);
        }

        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    @Override
    public MqServer addAccess(String accessKey, String accessSecretKey) {
        serverAccessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 启动
     */
    @Override
    public MqServer start(int port) throws Exception {

        //创建 SocketD 服务并配置（使用 tpc 通讯）
        server = SocketD.createServer("sd:tcp");

        if (serverConfigHandler != null) {
            server.config(serverConfigHandler);
        }

        server.config(c -> c.port(port)).listen(this);

        //持久化::服务启动之前
        persistent.onStartBefore();

        //启动
        server.start();

        //持久化::服务启动之后
        persistent.onStartAfter();

        return this;
    }

    /**
     * 保存
     */
    @Override
    public void save() {
        //持久化::保存时
        persistent.onSave();
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        //持久化::服务停止之前
        persistent.onStopBefore();

        //停止
        server.stop();

        //关闭队列
        List<MqTopicConsumerQueue> queueList = new ArrayList<>(topicConsumerMap.values());
        for (MqTopicConsumerQueue queue : queueList) {
            queue.close();
        }

        //持久化::服务停止之后
        persistent.onStopAfter();
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        if (serverAccessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                session.close();
                return;
            }

            if (accessSecretKey.equals(serverAccessMap.get(accessKey)) == false) {
                session.close();
                return;
            }
        }

        log.info("Server channel opened, session={}", session.sessionId());
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);

        log.info("Server channel closed, session={}", session.sessionId());

        //遍历这个会话身上的消费者身份（有些可能不是）
        //避免遍历 Set 时，出现 add or remove 而异常
        List<String> topicConsumerList = new ArrayList<>(session.attrMap().keySet());
        for (String topicConsumer : topicConsumerList) {
            MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

            //如果找到对应的队列（如果没有，表示这个属性不是消费者）
            if (topicConsumerQueue != null) {
                topicConsumerQueue.removeSession(session);
            }
        }
    }

    /**
     * 会话出错时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        if (log.isWarnEnabled()) {
            log.warn("Server channel error, session={}", session.sessionId(), error);
        }
    }

    @Override
    public Map<String, Set<String>> getSubscribeMap() {
        return Collections.unmodifiableMap(subscribeMap);
    }

    @Override
    public Map<String, MqTopicConsumerQueue> getTopicConsumerMap() {
        return Collections.unmodifiableMap(topicConsumerMap);
    }

    /**
     * 执行订阅
     */
    @Override
    public synchronized void subscribeDo(String topic, String consumer, Session session) {
        String topicConsumer = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER + consumer;

        //::1.构建订阅关系

        //以身份进行订阅(topic=>[topicConsumer])
        Set<String> topicConsumerSet = subscribeMap.get(topic);
        if (topicConsumerSet == null) {
            topicConsumerSet = new HashSet<>();
            subscribeMap.put(topic, topicConsumerSet);
        }

        topicConsumerSet.add(topicConsumer);

        //为身份建立队列(topicConsumer=>MqTopicConsumerQueue)
        MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);
        if (topicConsumerQueue == null) {
            topicConsumerQueue = new MqTopicConsumerQueueDefault(persistent, topic, consumer);
            topicConsumerMap.put(topicConsumer, topicConsumerQueue);
        }

        //::2.标识会话身份（从持久层恢复时，会话可能为 null）

        if (session != null) {
            log.info("Server channel subscribe topic={}, consumer={}, session={}", topic, consumer, session.sessionId());

            //会话添加身份（可以有多个不同的身份）
            session.attr(topicConsumer, "1");

            //加入主题消息队列
            topicConsumerQueue.addSession(session);
        }
    }

    /**
     * 执行取消订阅
     */
    @Override
    public synchronized void unsubscribeDo(String topic, String consumer, Session session) {
        String topicConsumer = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER + consumer;

        //1.获取身份建立队列(topicConsumer=>MqTopicConsumerQueue)
        MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

        //::2.移除会话身份（从持久层恢复时，会话可能为 null）

        if (session != null) {
            log.info("Server channel unsubscribe topic={}, consumer={}, session={}", topic, consumer, session.sessionId());

            //会话移除身份（可以有多个不同的身份）
            session.attrMap().remove(topicConsumer);

            //退出主题消息队列
            if (topicConsumerQueue != null) {
                topicConsumerQueue.removeSession(session);
            }
        }
    }

    /**
     * 执行交换
     *
     * @param topic   主题
     * @param message 消息源
     */
    @Override
    public void exchangeDo(String topic, Message message) {
        //复用解析
        String tid = message.meta(MqConstants.MQ_META_TID);
        int qos = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_QOS, "1"));
        long scheduled = Long.parseLong(message.metaOrDefault(MqConstants.MQ_META_SCHEDULED, "0"));

        //可能是非法消息
        if (Utils.isEmpty(tid)) {
            log.warn("The tid cannot be null, sid={}", message.sid());
            return;
        }

        //取出所有订阅的主题消息者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

                if (topicConsumerQueue != null) {
                    MqMessageHolder messageHolder = new MqMessageHolder(message, tid, qos, scheduled);
                    topicConsumerQueue.add(messageHolder);
                }
            }
        }
    }
}