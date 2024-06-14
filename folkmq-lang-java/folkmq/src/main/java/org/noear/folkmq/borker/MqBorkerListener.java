package org.noear.folkmq.borker;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.common.*;
import org.noear.snack.ONode;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.exception.SocketDAlarmException;
import org.noear.socketd.transport.core.*;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.MessageHandler;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.RunnableEx;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * 消息服务监听器
 *
 * @author noear
 * @since 1.0
 */
public class MqBorkerListener extends MqBorkerListenerBase implements MqBorkerInternal {
    protected final BrokerListener brokerListener = new BrokerListener();
    protected final MqQps qpsPublish = new MqQps();
    protected final MqQps qpsDistribute = new MqQps();
    protected final ScheduledFuture<?> qpsScheduled;

    @Override
    public MqQps getQpsDistribute() {
        return qpsDistribute;
    }

    @Override
    public MqQps getQpsPublish() {
        return qpsPublish;
    }

    public MqBorkerListener(boolean clusterMode) {
        //::初始化 Watcher 接口
        this.clusterMode = clusterMode;

        this.distributeThread = new Thread(this::distributeDo, "distributeThread");

        this.watcher = new MqWatcherDefault();
        this.watcher.init(this);

        this.qpsScheduled = RunUtils.delayAndRepeat(()->{
            qpsPublish.reset();
            qpsDistribute.reset();
        },5_000);

        //::初始化 BuilderListener(self) 的路由监听
        doOn(MqConstants.MQ_EVENT_SUBSCRIBE, (s, m) -> {
            //接收订阅指令
            onSubscribe(s, m);
            confirmDo(s, m);
        });

        doOn(MqConstants.MQ_EVENT_UNSUBSCRIBE, (s, m) -> {
            //接收取消订阅指令
            onUnsubscribe(s, m);
            confirmDo(s, m);
        });

        doOn(MqConstants.MQ_EVENT_PUBLISH, (s, m) -> {
            qpsPublish.record();

            MqMetasResolver mr = MqUtils.getOf(m);
            //接收发布指令
            boolean isTrans = mr.isTransaction(m);

            if (clusterMode) {
                //如果是经理模式，派发的消息不受内存限制
                m.putMeta(EntityMetas.META_X_UNLIMITED, "1");
            }

            if (isTrans) {
                //备份
                mr.bakExpiration(m, true);
                mr.bakScheduled(m, true);

                //存活为2小时，延后为1分钟
                mr.setExpiration(m, System.currentTimeMillis() + MqNextTime.TIME_1H);
                mr.setScheduled(m, System.currentTimeMillis() + MqNextTime.TIME_1M);

                //预备存储
                MqDraft draft = new MqDraft(mr, m);

                String queueName = draft.topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + MqConstants.MQ_TRAN_CONSUMER_GROUP;

                transactionMessageMap.put(draft.key, draft.topic);
                queueGetOrInit(draft.topic, MqConstants.MQ_TRAN_CONSUMER_GROUP, queueName);
                routingToQueueName(draft, queueName);
            } else {
                onPublish(s, m, mr);
            }

            confirmDo(s, m);
        });

        doOn(MqConstants.MQ_EVENT_PUBLISH2, (s, m) -> {
            //接收二段发布指令
            boolean isRollback = "1".equals(m.meta(MqConstants.MQ_META_ROLLBACK));
            String[] keyAry = m.dataAsString().split(",");
            boolean confirmIsOk = true;
            String confirmHint = "";

            if (isRollback == false) {
                //如果不是回滚，则先做检测（是否提交的 key 都在）
                for (String key : keyAry) {
                    if (transactionMessageMap.containsKey(key) == false) {
                        confirmIsOk = false;

                        if (confirmHint.length() > 0) {
                            confirmHint += "," + key;
                        } else {
                            confirmHint = "Transaction messages have failed to be published: " + key;
                        }
                    }
                }
            }

            if (confirmIsOk) {
                for (String key : keyAry) {
                    String topic = transactionMessageMap.remove(key);
                    if (topic != null) {
                        String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + MqConstants.MQ_TRAN_CONSUMER_GROUP;
                        MqQueue queue = getQueue(queueName);
                        if (queue != null) {
                            queue.affirmAt(key, isRollback);
                        }
                    }
                }
            }

            confirmDo(s, m, confirmIsOk, confirmHint);
        });

        doOn(MqConstants.MQ_EVENT_UNPUBLISH, (s, m) -> {
            //接收取消发布指令
            onUnpublish(s, m);
            confirmDo(s, m);
        });

        doOn(MqConstants.MQ_EVENT_SAVE, (s, m) -> {
            //接收保存指令
            RunUtils.asyncAndTry(() -> {
                save();
                confirmDo(s, m);
            });
        });

        doOn(MqConstants.MQ_EVENT_REQUEST, (s, m) -> {
            qpsPublish.record();

            String atName = m.atName();

            //单发模式（给同名的某个玩家，轮询负截均衡）
            Session responder = brokerListener.getPlayerAny(atName, s, m);
            if (responder != null && responder.isValid()) {
                //转发消息
                try {
                    qpsDistribute.record();
                    brokerListener.forwardToSession(s, m, responder);
                } catch (Throwable e) {
                    s.sendAlarm(m, "Broker: forward '@" + atName + "' error: " + e.getMessage());
                }
            } else {
                s.sendAlarm(m, "Broker: don't have '@" + atName + "' session");
            }
        });
    }

    @Override
    public void onSend(Session session, Message message) {
        if (MqConstants.MQ_EVENT_DISTRIBUTE.equals(message.event())) {
            MqMetasResolver mr = MqUtils.getOf(message);
            if (mr.version() >= 3) {
                session.config().getStreamManger().removeStream(message.sid());
            }
        }
    }

    @Override
    public void onReply(Session session, Message message) {
        if (MqConstants.MQ_EVENT_DISTRIBUTE.equals(message.event())) {
            MqMetasResolver mr = MqUtils.getOf(message);
            if (mr.version() >= 3) {
                String key = mr.getKey(message);
                String topic = mr.getTopic(message);
                String consumerGroup = mr.getConsumerGroup(message);

                String queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
                MqQueue queue = getQueue(queueName);
                if (queue != null) {
                    int ack = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_ACK, "0"));
                    queue.acknowledgeAt(key, ack);
                }
            }
        }
    }

    /**
     * 配置监视器
     */
    public MqBorkerListener watcher(MqWatcher watcher) {
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
    public MqBorkerListener addAccess(String accessKey, String accessSecretKey) {
        serverAccessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    public MqBorkerListener addAccessAll(Map<String, String> accessMap) {
        if (accessMap != null) {
            serverAccessMap.putAll(accessMap);
        }
        return this;
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
     * 启动
     */
    public void start(RunnableEx<Exception> onStart) throws Exception {
        //观察者::服务启动之前
        watcher.onStartBefore();

        //启动
        if (onStart != null) {
            onStart.run();
        }
        distributeThread.start();

        //观察者::服务启动之后
        watcher.onStartAfter();

        //标为已启动
        isStarted.set(true);
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

        if (qpsScheduled != null) {
            qpsScheduled.cancel(true);
        }

        //标为已停止
        isStarted.set(false);
    }

    /**
     * 会话打开时
     */
    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        //返馈版本号
        session.handshake().outMeta(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString());

        if (clusterMode) {
            //申请加入 //不要用 sendAndRequest
            session.send(MqConstants.MQ_EVENT_JOIN, new StringEntity("").metaPut(EntityMetas.META_X_UNLIMITED, "1"));

            log.info("Broker: {} channel opened, sessionId={}", chanelType(), session.sessionId());
        } else {
            //鉴权
            if (this.auth(session) == false) {
                session.close();
                return;
            }

            log.info("Broker: {} channel opened, sessionId={}", chanelType(), session.sessionId());
        }

        //添加会话，用于停止通知
        sessionAllMap.put(session.sessionId(), session);

        //增加经理人支持
        brokerListener.onOpen(session);
    }

    /**
     * 鉴权失败返回false
     *
     * @param session
     * @return
     */
    protected boolean auth(Session session) {
        if (serverAccessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                return false;
            }

            if (accessSecretKey.equals(serverAccessMap.get(accessKey)) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * 会话关闭时
     */
    @Override
    public void onClose(Session session) {
        super.onClose(session);


        log.info("Broker: {} channel closed, sessionId={}", chanelType(), session.sessionId());


        //遍历会话绑定的队列 //线程安全处理
        List<String> queueNameList = new ArrayList<>(session.attrMap().keySet());
        for (String queueName : queueNameList) {
            MqQueue queue = queueMap.get(queueName);

            //如果找到对应的队列
            if (queue != null) {
                queue.sessionRemove(session);
            }
        }

        //增加经理人支持
        brokerListener.onClose(session);
    }

    /**
     * 会话出错时
     */
    @Override
    public void onError(Session session, Throwable error) {
        super.onError(session, error);

        if (log.isWarnEnabled()) {
            if (error instanceof SocketDAlarmException) {
                SocketDAlarmException alarmException = (SocketDAlarmException) error;

                log.warn("Broker: {} channel error, sessionId={}, from={}", chanelType(), session.sessionId(), alarmException.getAlarm(), error);
            } else {
                log.warn("Broker: {} channel error, sessionId={}", chanelType(), session.sessionId(), error);
            }
        }
    }

    @Override
    public void doOnEvent(String event, MessageHandler handler) {
        doOn(event, handler);
    }

    protected void onSubscribe(Session s, Message m) throws IOException {
        String is_batch = m.meta(MqConstants.MQ_META_BATCH);

        if ("1".equals(is_batch)) {
            ONode oNode = ONode.loadStr(m.dataAsString());
            Map<String, Collection<String>> subscribeData = oNode.toObject();
            if (subscribeData != null) {
                for (Map.Entry<String, Collection<String>> kv : subscribeData.entrySet()) {
                    for (String queueName : kv.getValue()) {
                        String consumerGroup = queueName.split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP)[1];

                        //观察者::订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
                        watcher.onSubscribe(kv.getKey(), consumerGroup, s);

                        //执行订阅
                        subscribeDo(kv.getKey(), consumerGroup, s);
                    }
                }
            }
        } else {
            String topic = m.meta(MqConstants.MQ_META_TOPIC);
            String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

            //观察者::订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
            watcher.onSubscribe(topic, consumerGroup, s);

            //执行订阅
            subscribeDo(topic, consumerGroup, s);
        }
    }

    protected void onUnsubscribe(Session s, Message m) throws IOException {
        String topic = m.meta(MqConstants.MQ_META_TOPIC);
        String consumerGroup = m.meta(MqConstants.MQ_META_CONSUMER_GROUP);

        //观察者::取消订阅时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
        watcher.onUnSubscribe(topic, consumerGroup, s);

        //执行取消订阅
        unsubscribeDo(topic, consumerGroup, s);
    }

    private void onPublish(Session s, Message m, MqMetasResolver mr) throws IOException {
        if (m == null) {
            return;
        }

        //观察者::发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
        watcher.onPublish(m);

        //执行交换
        routingDo(mr, m);
    }

    private void onUnpublish(Session s, Message m) throws IOException {
        //观察者::取消发布时（适配时，可选择同步或异步。同步可靠性高，异步性能好）
        watcher.onUnPublish(m);

        //执行交换
        unRoutingDo(m);
    }

    private void confirmDo(Session s, Message m) throws IOException {
        confirmDo(s, m, true, "");
    }

    private void confirmDo(Session s, Message m, boolean isOk, String hint) throws IOException {
        //答复（以支持同步的原子性需求。同步或异步，由用户按需控制）
        if (m.isRequest() || m.isSubscribe()) {
            //发送“确认”，表示服务端收到了
            if (s.isValid()) {
                //如果会话仍有效，则答复（有可能会半路关掉）
                s.replyEnd(m, new StringEntity(hint).metaPut(MqConstants.MQ_META_CONFIRM, (isOk ? "1" : "0")));
            }
        }
    }
}