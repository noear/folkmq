package org.noear.folkmq.broker.admin.dso;

import org.noear.folkmq.broker.admin.model.QueueVo;
import org.noear.folkmq.broker.common.ConfigNames;
import org.noear.folkmq.broker.mq.BrokerListenerFolkmq;
import org.noear.folkmq.common.MqConstants;
import org.noear.snack.ONode;
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
    private BrokerListenerFolkmq brokerListener;

    private Set<String> queueSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, QueueVo> queueVoMap = new ConcurrentHashMap<>();
    private Map<String, QueueVo> queueVoMap2 = new ConcurrentHashMap<>();
    private Object QUEUE_LOCK = new Object();

    private ScheduledFuture<?> scheduledFuture;

    public List<QueueVo> getQueueListVo() {
        List<QueueVo> list = new ArrayList<>();

        for (String queue : new ArrayList<>(queueSet)) {
            QueueVo queueVo = queueVoMap.get(queue);
            if (queueVo == null) {
                queueVo = new QueueVo();
                queueVo.queue = queue;
            }

            list.add(queueVo);
        }

        return list;
    }

    @Override
    public void start() throws Throwable {
        delay();
    }

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
            Collection<Session> tmp = brokerListener.getPlayerAll(MqConstants.BROKER_AT_SERVER);
            if (tmp == null) {
                return;
            }

            //一种切换效果。把上次收集的效果切换给当前的。然后重新开始收集
            queueVoMap.clear();
            queueVoMap.putAll(queueVoMap2);
            queueVoMap2.clear();

            List<Session> sessions = new ArrayList<>(tmp);
            for (Session session : sessions) {
                try {
                    session.sendAndRequest(MqConstants.ADMIN_VIEW_QUEUE, new StringEntity(""), r -> {
                        String json = r.dataAsString();
                        List<QueueVo> list = ONode.loadStr(json).toObjectList(QueueVo.class);
                        addQueueVo(list, queueVoMap2);
                    });
                } catch (Throwable e) {
                    log.warn("Cmd 'admin.view.queue' call error", e);
                }
            }
        }finally {
            delay();
        }
    }

    private void addQueueVo(List<QueueVo> list, Map<String, QueueVo> coll) {
        synchronized (QUEUE_LOCK) {
            for (QueueVo queueVo : list) {
                if(Utils.isEmpty(queueVo.queue)){
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
