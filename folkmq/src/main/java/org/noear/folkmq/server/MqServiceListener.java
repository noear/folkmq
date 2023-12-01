package org.noear.folkmq.server;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.exception.SocketdAlarmException;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.EventListener;
import org.noear.socketd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息服务监听器
 *
 * @author noear
 * @since 1.0
 */
public class MqServiceListener extends EventListener implements MqServiceInternal {
    private static final Logger log = LoggerFactory.getLogger(MqServerDefault.class);

    private Object SUBSCRIBE_LOCK = new Object();

    //服务端访问账号
    private Map<String, String> serverAccessMap = new ConcurrentHashMap<>();
    //观察者
    private MqWatcher watcher;

    //订阅关系表(topicConsumer=>MqTopicConsumerQueue)
    private Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    //主题消费队列表(topicConsumer=>MqTopicConsumerQueue)
    private Map<String, MqTopicConsumerQueue> topicConsumerMap = new ConcurrentHashMap<>();

    private boolean brokerMode;


    public MqServiceListener(boolean brokerMode) {
        //::初始化 Watcher 接口
        this.brokerMode = brokerMode;

        this.watcher = new MqWatcherDefault();
        this.watcher.init(this);

        //::初始化 BuilderListener(self) 的路由监听

        //接收订阅指令
        on(MqConstants.MQ_EVENT_SUBSCRIBE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumer = m.meta(MqConstants.MQ_META_CONSUMER);

            //观察者::订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onSubscribe(topic, consumer, s);

            //观察后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
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

            //观察者::取消订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onUnSubscribe(topic, consumer, s);

            //观察后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) {
                //发送“确认”，表示服务端收到了
                s.replyEnd(m, new StringEntity(""));
            }

            //执行取消订阅
            unsubscribeDo(topic, consumer, s);
        });

        //接收发布指令
        on(MqConstants.MQ_EVENT_PUBLISH, (s, m) -> {
            //观察者::发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onPublish(m);

            //观察后，再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) { //此判断兼容 Qos0, Qos1
                //发送“确认”，表示服务端收到了
                s.replyEnd(m, new StringEntity(""));
            }

            //执行交换
            exchangeDo(m);
        });

        //接收保存指令
        on(MqConstants.MQ_EVENT_SAVE, (s, m) -> {
            save();
        });
    }

    public MqServiceListener watcher(MqWatcher watcher) {
        if (watcher != null) {
            this.watcher = watcher;
            this.watcher.init(this);
        }

        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    public MqServiceListener addAccess(String accessKey, String accessSecretKey) {
        serverAccessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    public MqServiceListener addAccessAll(Map<String, String> accessMap) {
        if (accessMap != null) {
            serverAccessMap.putAll(accessMap);
        }
        return this;
    }

    /**
     * 启动
     */
    public void start(OnStart onStart) throws Exception {

        //观察者::服务启动之前
        watcher.onStartBefore();

        //启动
        onStart.run();

        //观察者::服务启动之后
        watcher.onStartAfter();
    }

    /**
     * 保存
     */
    @Override
    public void save() {
        //观察者::保存时
        watcher.onSave();
    }

    /**
     * 停止
     */
    public void stop(Runnable onStop) {
        //观察者::服务停止之前
        watcher.onStopBefore();

        //停止
        onStop.run();

        //观察者::服务停止之后
        watcher.onStopAfter();

        //关闭队列
        List<MqTopicConsumerQueue> queueList = new ArrayList<>(topicConsumerMap.values());
        for (MqTopicConsumerQueue queue : queueList) {
            queue.close();
        }
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        if (brokerMode) {
            return;
        }

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

        log.info("Server channel opened, sessionId={}", session.sessionId());
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);

        if (brokerMode) {
            return;
        }

        log.info("Server channel closed, sessionId={}", session.sessionId());

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
            if (error instanceof SocketdAlarmException) {
                SocketdAlarmException alarmException = (SocketdAlarmException) error;
                log.warn("Server channel error, sessionId={}, from={}", session.sessionId(), alarmException.getFrom(), error);
            } else {
                log.warn("Server channel error, sessionId={}", session.sessionId(), error);
            }
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
    public void subscribeDo(String topic, String consumer, Session session) {
        String topicConsumer = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER + consumer;

        synchronized (SUBSCRIBE_LOCK) {
            //::1.构建订阅关系

            //以身份进行订阅(topic=>[topicConsumer])
            Set<String> topicConsumerSet = subscribeMap.computeIfAbsent(topic, n-> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            topicConsumerSet.add(topicConsumer);

            //为身份建立队列(topicConsumer=>MqTopicConsumerQueue)
            MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);
            if (topicConsumerQueue == null) {
                topicConsumerQueue = new MqTopicConsumerQueueDefault(watcher, topic, consumer);
                topicConsumerMap.put(topicConsumer, topicConsumerQueue);
            }

            //::2.标识会话身份（从持久层恢复时，会话可能为 null）

            if (session != null) {
                log.info("Server channel subscribe topic={}, consumer={}, sessionId={}", topic, consumer, session.sessionId());

                //会话添加身份（可以有多个不同的身份）
                session.attr(topicConsumer, "1");

                //加入主题消息队列
                topicConsumerQueue.addSession(session);
            }
        }
    }

    /**
     * 执行取消订阅
     */
    @Override
    public void unsubscribeDo(String topic, String consumer, Session session) {
        if (session == null) {
            return;
        }

        log.info("Server channel unsubscribe topic={}, consumer={}, sessionId={}", topic, consumer, session.sessionId());

        String topicConsumer = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER + consumer;

        //1.获取身份建立队列(topicConsumer=>MqTopicConsumerQueue)
        MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

        //2.移除会话身份（从持久层恢复时，会话可能为 null）
        session.attrMap().remove(topicConsumer);

        //退出主题消息队列
        if (topicConsumerQueue != null) {
            topicConsumerQueue.removeSession(session);
        }
    }

    /**
     * 执行交换
     */
    @Override
    public void exchangeDo(Message message) {
        String tid = message.meta(MqConstants.MQ_META_TID);
        //可能是非法消息
        if (Utils.isEmpty(tid)) {
            log.warn("The tid cannot be null, sid={}", message.sid());
            return;
        }

        //复用解析
        String topic = message.meta(MqConstants.MQ_META_TOPIC);
        int qos = "0".equals(message.meta(MqConstants.MQ_META_QOS)) ? 0 : 1;
        int times = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
        long scheduled = 0;
        String scheduledStr = message.meta(MqConstants.MQ_META_SCHEDULED);
        if (Utils.isNotEmpty(scheduledStr)) {
            scheduled = Long.parseLong(scheduledStr);
        }


        //取出所有订阅的主题消息者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                exchangeDo(topicConsumer, message, tid, qos, times, scheduled);
            }
        }
    }

    public void exchangeDo(String topicConsumer, Message message, String tid, int qos, int times, long scheduled) {
        MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

        if (topicConsumerQueue != null) {
            MqMessageHolder messageHolder = new MqMessageHolder(topicConsumerQueue.getConsumer(), message, tid, qos, times, scheduled);
            topicConsumerQueue.add(messageHolder);
        }
    }
}