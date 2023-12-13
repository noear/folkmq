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

    //订阅关系(topic=>[queueName]) //queueName='topic#consumer'
    private Map<String, Set<String>> subscribeMap = new ConcurrentHashMap<>();
    //队列字典(queueName=>Queue)
    private Map<String, MqQueue> queueMap = new ConcurrentHashMap<>();

    //派发线程
    private Thread distributeThread;

    private boolean brokerMode;


    public MqServiceListener(boolean brokerMode) {
        //::初始化 Watcher 接口
        this.brokerMode = brokerMode;

        this.distributeThread = new Thread(this::distributeDo, "distributeThread");

        this.watcher = new MqWatcherDefault();
        this.watcher.init(this);

        //::初始化 BuilderListener(self) 的路由监听

        //接收订阅指令
        on(MqConstants.MQ_EVENT_SUBSCRIBE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            //观察者::订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onSubscribe(topic, consumerGroup, s);

            //执行订阅
            subscribeDo(topic, consumerGroup, s);

            //答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) {
                //发送“确认”，表示服务端收到了
                if (s.isValid()) {
                    //如果会话仍有效，则答复（有可能会半路关掉）
                    s.replyEnd(m, new StringEntity("").meta(MqConstants.MQ_META_CONFIRM, "1"));
                }
            }
        });

        //接收取消订阅指令
        on(MqConstants.MQ_EVENT_UNSUBSCRIBE, (s, m) -> {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            //观察者::取消订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onUnSubscribe(topic, consumerGroup, s);

            //执行取消订阅
            unsubscribeDo(topic, consumerGroup, s);

            //答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) {
                //发送“确认”，表示服务端收到了
                if (s.isValid()) {
                    //如果会话仍有效，则答复（有可能会半路关掉）
                    s.replyEnd(m, new StringEntity("").meta(MqConstants.MQ_META_CONFIRM, "1"));
                }
            }
        });

        //接收发布指令
        on(MqConstants.MQ_EVENT_PUBLISH, (s, m) -> {
            //观察者::发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onPublish(m);

            //执行交换
            routingDo(m);

            //再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) { //此判断兼容 Qos0, Qos1
                //发送“确认”，表示服务端收到了
                if (s.isValid()) {
                    //如果会话仍有效，则答复（有可能会半路关掉）
                    s.replyEnd(m, new StringEntity("").meta(MqConstants.MQ_META_CONFIRM, "1"));
                }
            }
        });

        //接收取消发布指令
        on(MqConstants.MQ_EVENT_UNPUBLISH, (s,m)->{
            //观察者::取消发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onUnPublish(m);

            //执行交换
            unRoutingDo(m);

            //再答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
            if (m.isRequest() || m.isSubscribe()) { //此判断兼容 Qos0, Qos1
                //发送“确认”，表示服务端收到了
                if (s.isValid()) {
                    //如果会话仍有效，则答复（有可能会半路关掉）
                    s.replyEnd(m, new StringEntity("").meta(MqConstants.MQ_META_CONFIRM, "1"));
                }
            }
        });

        //接收保存指令
        on(MqConstants.MQ_EVENT_SAVE, (s, m) -> {
            save();

            if (m.isRequest() || m.isSubscribe()) { //此判断兼容 Qos0, Qos1
                //发送“确认”，表示服务端收到了
                if (s.isValid()) {
                    //如果会话仍有效，则答复（有可能会半路关掉）
                    s.replyEnd(m, new StringEntity("").meta(MqConstants.MQ_META_CONFIRM, "1"));
                }
            }
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
        if (onStart != null) {
            onStart.run();
        }
        distributeThread.start();

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
        if (onStop != null) {
            onStop.run();
        }
        distributeThread.interrupt();

        //观察者::服务停止之后
        watcher.onStopAfter();

        //关闭队列
        List<MqQueue> queueList = new ArrayList<>(queueMap.values());
        for (MqQueue queue : queueList) {
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
            log.info("Broker channel opened, sessionId={}", session.sessionId());
        } else {
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

            log.info("Client channel opened, sessionId={}", session.sessionId());
        }
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

        //遍历会话绑定的队列 //线程安全处理
        List<String> queueNameList = new ArrayList<>(session.attrMap().keySet());
        for (String queueName : queueNameList) {
            MqQueue queue = queueMap.get(queueName);

            //如果找到对应的队列
            if (queue != null) {
                queue.removeSession(session);
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
    public Map<String, MqQueue> getQueueMap() {
        return Collections.unmodifiableMap(queueMap);
    }

    /**
     * 执行订阅
     */
    @Override
    public void subscribeDo(String topic, String consumerGroup, Session session) {
        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        synchronized (SUBSCRIBE_LOCK) {
            //::1.构建订阅关系

            //建立订阅关系(topic=>[queueName]) //queueName='topic#consumer'
            Set<String> queueNameSet = subscribeMap.computeIfAbsent(topic, n -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            queueNameSet.add(queueName);

            //队列映射关系(queueName=>Queue)
            MqQueue queue = queueMap.get(queueName);
            if (queue == null) {
                queue = new MqQueueDefault(watcher, topic, consumerGroup, queueName);
                queueMap.put(queueName, queue);
            }

            //::2.标识会话身份（从持久层恢复时，会话可能为 null）

            if (session != null) {
                log.info("Server channel subscribe topic={}, consumerGroup={}, sessionId={}", topic, consumerGroup, session.sessionId());

                //会话绑定队列（可以绑定多个队列）
                session.attr(queueName, "1");

                //加入队列会话
                queue.addSession(session);
            }
        }
    }

    /**
     * 执行取消订阅
     */
    @Override
    public void unsubscribeDo(String topic, String consumerGroup, Session session) {
        if (session == null) {
            return;
        }

        log.info("Server channel unsubscribe topic={}, consumerGroup={}, sessionId={}", topic, consumerGroup, session.sessionId());

        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        //1.获取相关队列
        MqQueue queue = queueMap.get(queueName);

        //2.移除队列绑定
        session.attrMap().remove(queueName);

        //3.退出队列会话
        if (queue != null) {
            queue.removeSession(session);
        }
    }

    /**
     * 执行路由
     */
    @Override
    public void routingDo(Message message) {
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
        } else {
            //默认为当前ms（相对于后面者，有个排序作用）
            scheduled = System.currentTimeMillis();
        }


        //取出所有订阅的主题消费者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                routingDo(topicConsumer, message, tid, qos, times, scheduled);
            }
        }
    }

    /**
     * 执行路由
     */
    public void routingDo(String queueName, Message message, String tid, int qos, int times, long scheduled) {
        MqQueue queue = queueMap.get(queueName);

        if (queue != null) {
            MqMessageHolder messageHolder = new MqMessageHolder(queueName, queue.getConsumerGroup(), message, tid, qos, times, scheduled);
            queue.add(messageHolder);
        }
    }

    /**
     * 执行取消路由
     */
    public void unRoutingDo(Message message) {
        String tid = message.meta(MqConstants.MQ_META_TID);
        //可能是非法消息
        if (Utils.isEmpty(tid)) {
            log.warn("The tid cannot be null, sid={}", message.sid());
            return;
        }

        //复用解析
        String topic = message.meta(MqConstants.MQ_META_TOPIC);

        //取出所有订阅的主题消费者
        Set<String> topicConsumerSet = subscribeMap.get(topic);

        if (topicConsumerSet != null) {
            //避免遍历 Set 时，出现 add or remove 而异常
            List<String> topicConsumerList = new ArrayList<>(topicConsumerSet);

            for (String topicConsumer : topicConsumerList) {
                MqQueue queue = queueMap.get(topicConsumer);
                queue.removeAt(tid);
            }
        }
    }

    private void distributeDo() {
        while (!distributeThread.isInterrupted()) {
            try {
                int count = 0;

                List<MqQueue> queueList = new ArrayList<>(queueMap.values());
                for (MqQueue queue : queueList) {
                    try {
                        if (queue.distribute()) {
                            count++;
                        }
                    } catch (Throwable e) {
                        if (log.isWarnEnabled()) {
                            log.warn("MqQueue take error, queue={}", queue.getQueueName(), e);
                        }
                    }
                }

                if (count == 0) {
                    //一点消息都没有，就修复下
                    Thread.sleep(100);
                }
            } catch (Throwable e) {
                if (e instanceof InterruptedException == false) {
                    if (log.isWarnEnabled()) {
                        log.warn("MqQueue distribute error", e);
                    }
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("MqQueue take stoped!");
        }
    }
}