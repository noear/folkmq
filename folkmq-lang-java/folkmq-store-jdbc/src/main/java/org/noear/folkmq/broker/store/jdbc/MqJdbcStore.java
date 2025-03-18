package org.noear.folkmq.broker.store.jdbc;

import org.noear.folkmq.broker.*;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasResolver;
import org.noear.folkmq.common.MqUtils;
import org.noear.folkmq.utils.SnowflakeId;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.MessageBuilder;
import org.noear.socketd.utils.StrUtils;
import org.noear.wood.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author noear
 * @since 1.8
 */
public class MqJdbcStore extends MqStoreBase {
    protected static final Logger log = LoggerFactory.getLogger(MqJdbcStore.class);

    //服务端引用
    private MqBorkerInternal serverRef;
    private DbContext db;

    //正在保持中
    private final AtomicBoolean inSaveProcess = new AtomicBoolean(false);
    //是否已启动
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public MqJdbcStore(DataSource dataSource) {
       this.db = new DbContext(dataSource);

    }

    @Override
    public String getName() {
        return "mapdb";
    }

    @Override
    public void init(MqBorkerInternal serverInternal) {
        this.serverRef = serverInternal;
    }

    @Override
    public void onStartBefore() {
        isStarted.set(false);

        try {
            loadSubscribeMap();
            loadQueue();
        } finally {
            isStarted.set(true);
        }
    }

    /**
     * 加载订阅关系（确保线程安全）
     */
    private void loadSubscribeMap() {
        try {
            List<SubscribeDoc> subscribeDocList = db.table("subscribe").selectList("*", SubscribeDoc.class);

            for (SubscribeDoc subs : subscribeDocList) {
                String consumerGroup = subs.queueName.split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP)[1];
                serverRef.subscribeDo(subs.topic, consumerGroup, null);
            }

            log.info("Server persistent load subscribeMap completed");
        } catch (Exception e) {
            log.warn("Server persistent load subscribeMap failed", e);
        }
    }

    /**
     * 加载主题消费队列记录（确保线程安全）
     */
    private void loadQueue() {
        Map<String, Set<String>> subscribeMap = serverRef.getSubscribeMap();
        if (subscribeMap.size() == 0) {
            return;
        }

        List<String> topicList = new ArrayList<>(subscribeMap.keySet());

        Set<String> queueNameSet = new HashSet<>();
        for (String topic : topicList) {
            Set<String> tmp = subscribeMap.get(topic);
            if (tmp != null) {
                queueNameSet.addAll(tmp);
            }
        }

        for (String queueName : queueNameSet) {
            try {
                loadQueue1(queueName);

                log.info("Server persistent load messageQueue completed, queueName={}", queueName);
            } catch (Exception e) {
                log.warn("Server persistent load messageQueue failed, queueName={}", queueName, e);
            }
        }
    }

    private boolean loadQueue1(String queueName) throws Exception {
        List<MessageDoc> msgList = db.table("message")
                .whereEq("queueName", queueName)
                .selectList("*", MessageDoc.class);

        for (MessageDoc msg : msgList) {
            if (msg.data == null) {
                continue;
            }

            EntityDefault entity = new EntityDefault();
            entity.dataSet(msg.data.getBytes(StandardCharsets.UTF_8));

            entity.metaStringSet(msg.metaString);
            Message message = new MessageBuilder()
                    .sid(StrUtils.guid())
                    .flag(Flags.Message)
                    .entity(entity)
                    .build();

            MqMetasResolver mr = MqUtils.getOf(message);
            MqDraft draft = new MqDraft(mr, message);

            MqQueue queue = serverRef.getQueue(queueName);
            serverRef.routingToQueueDo(draft, queue, msg.id);
        }

        return true;
    }


    /// ///////////////////////////////////////

    @Override
    public void onStopAfter() {
        isStarted.set(false);
    }

    @Override
    public void onSubscribe(String topic, String consumerGroup, Session session) {
        SubscribeDoc doc = new SubscribeDoc();
        doc.topic = topic;
        doc.queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        try {
            db.table("subscribe").set("topic", doc.topic).set("queueName", doc.queueName)
                    .insertBy("queueName");
        } catch (Throwable ex) {
            log.error("onSubscribe error", ex);
        }
    }

    @Override
    public void onRouting(MqMessageHolder messageHolder) {
        if (messageHolder.getId() > 0L) {
            return;
        }

        MessageDoc doc = new MessageDoc();
        doc.id = SnowflakeId.DEFAULT.nextId();
        doc.ver = 2;
        doc.queueName = messageHolder.getQueueName();
        doc.metaString = messageHolder.getEntity().metaString();
        doc.data = messageHolder.getEntity().dataAsString();

        try {
            db.table("message")
                    .set("id", doc.id)
                    .set("ver", doc.ver)
                    .set("queueName", doc.queueName)
                    .set("metaString", doc.metaString)
                    .set("data", doc.data)
                    .insertBy("id");

            messageHolder.setId(doc.id);
        } catch (Throwable ex) {
            log.error("onRouting error", ex);
        }
    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        if (isOk == false) {
            try {
                db.table("message")
                        .set("metaString", messageHolder.getEntity().metaString())
                        .whereEq("id", messageHolder.getId())
                        .update();
            } catch (Throwable ex) {
                log.error("onAcknowledge error", ex);
            }
        }
    }

    @Override
    public void onRemove(String topic, String consumerGroup, MqMessageHolder messageHolder) {
        if (messageHolder.getId() > 0L) {
            try {
                db.table("message")
                        .whereEq("id", messageHolder.getId())
                        .delete();
            } catch (Throwable ex) {
                log.error("onRemove error", ex);
            }
        }
    }
}