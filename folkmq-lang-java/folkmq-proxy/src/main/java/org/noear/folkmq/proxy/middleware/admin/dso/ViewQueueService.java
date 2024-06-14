package org.noear.folkmq.proxy.middleware.admin.dso;

import org.noear.folkmq.proxy.middleware.admin.model.QueueVo;
import org.noear.folkmq.proxy.middleware.admin.model.ServerInfoVo;
import org.noear.folkmq.proxy.middleware.common.ConfigNames;
import org.noear.folkmq.proxy.middleware.mq.FolkmqProxyListener;
import org.noear.folkmq.common.MqConstants;
import org.noear.snack.ONode;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.EntityMetas;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.noear.solon.core.util.RunUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author noear
 * @since 1.0
 */
@Component
public class ViewQueueService implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(ViewQueueService.class);

    @Inject
    private FolkmqProxyListener brokerListener;

    private Set<String> queueSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, QueueVo> queueVoMap = new ConcurrentHashMap<>();
    private Map<String, QueueVo> queueVoMapTmp = new ConcurrentHashMap<>();
    private Object QUEUE_LOCK = new Object();

    private ScheduledFuture<?> scheduledFuture;

    public List<QueueVo> getQueueListVo() {
        List<QueueVo> list = new ArrayList<>();

        for (String queueName : new ArrayList<>(queueSet)) {
            QueueVo queueVo = queueVoMap.get(queueName);
            if (queueVo == null) {
                queueVo = new QueueVo();//初始化
                queueVo.queue = queueName;
            }

            //随时刷新
            queueVo.sessionCount = brokerListener.getPlayerCount(queueName);
            list.add(queueVo);
        }

        return list;
    }


    public QueueVo getQueueVo(String queueName) {
        QueueVo queueVo = queueVoMap.get(queueName);
        if (queueVo != null) {
            queueVo.sessionCount = brokerListener.getPlayerCount(queueName);
        }

        return queueVo;
    }

    public List<String> getQueueSessionList(String queueName) throws IOException {
        List<String> list = new ArrayList<>();

        Collection<Session> sessionList = brokerListener.getPlayerAll(queueName);
        if (sessionList != null) {
            List<Session> sessions = new ArrayList<>(sessionList);
            for (Session s1 : sessions) {
                list.add(s1.remoteAddress().toString());

                //不超过99
                if (list.size() == 99) {
                    break;
                }
            }
        }

        return list;
    }

    public void removeQueueVo(String queueName) {
        queueVoMap.remove(queueName);
        queueVoMapTmp.remove(queueName);
        queueSet.remove(queueName);
    }

    @Override
    public void start() throws Throwable {
        delay();
    }

    /**
     * 延时处理
     */
    private void delay() {
        long sync_time_millis = Integer.parseInt(Solon.cfg().get(
                ConfigNames.folkmq_view_queue_syncInterval,
                ConfigNames.folkmq_view_queue_syncInterval_def));

        if (sync_time_millis > 0) {
            scheduledFuture = RunUtil.delay(this::delayDo, sync_time_millis);
        }
    }


    private void delayDo() {
        try {
            Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.PROXY_AT_BROKER);
            if (tmp == null) {
                return;
            }

            //一种切换效果。把上次收集的效果切换给当前的。然后重新开始收集
            queueVoMap.clear();
            queueVoMap.putAll(queueVoMapTmp);
            queueVoMapTmp.clear();

            List<Session> sessions = new ArrayList<>(tmp);
            Entity reqEntity = new StringEntity("").metaPut(EntityMetas.META_X_UNLIMITED, "1");

            for (Session session : sessions) {
                try {
                    session.sendAndRequest(MqConstants.ADMIN_VIEW_QUEUE, reqEntity).thenReply(r -> {
                        String json = r.dataAsString();
                        List<QueueVo> list = ONode.loadStr(json).toObjectList(QueueVo.class);
                        addQueueVo(list, queueVoMapTmp);
                    }).thenError(err -> {
                        log.debug(MqConstants.ADMIN_VIEW_QUEUE + " request failed", err);
                    });

                    session.sendAndRequest(MqConstants.ADMIN_VIEW_INSTANCE, reqEntity).thenReply(r -> {
                        String json = r.dataAsString();
                        ServerInfoVo infoVo = ONode.loadStr(json).toObject(ServerInfoVo.class);
                        session.attrPut("ServerInfoVo", infoVo);
                    }).thenError(err -> {
                        log.debug(MqConstants.ADMIN_VIEW_INSTANCE + " request failed", err);
                    });
                } catch (Throwable e) {
                    log.warn("Cmd 'admin.view.queue' call error", e);
                }
            }
        } finally {
            delay();
        }
    }

    private void addQueueVo(List<QueueVo> list, Map<String, QueueVo> coll) {
        synchronized (QUEUE_LOCK) {
            for (QueueVo queueVo : list) {
                if (Utils.isEmpty(queueVo.queue)) {
                    continue;
                }

                queueSet.add(queueVo.queue);

                QueueVo stat = coll.computeIfAbsent(queueVo.queue, n -> new QueueVo());

                stat.queue = queueVo.queue;
                stat.messageCount += queueVo.messageCount;
                stat.messageDelayedCount1 += queueVo.messageDelayedCount1;
                stat.messageDelayedCount2 += queueVo.messageDelayedCount2;
                stat.messageDelayedCount3 += queueVo.messageDelayedCount3;
                stat.messageDelayedCount4 += queueVo.messageDelayedCount4;
                stat.messageDelayedCount5 += queueVo.messageDelayedCount5;
                stat.messageDelayedCount6 += queueVo.messageDelayedCount6;
                stat.messageDelayedCount7 += queueVo.messageDelayedCount7;
                stat.messageDelayedCount8 += queueVo.messageDelayedCount8;

                String topic = queueVo.queue.split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP)[0];
                brokerListener.subscribeDo(null, topic, queueVo.queue);
            }
        }
    }

    @Override
    public void stop() throws Throwable {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
}