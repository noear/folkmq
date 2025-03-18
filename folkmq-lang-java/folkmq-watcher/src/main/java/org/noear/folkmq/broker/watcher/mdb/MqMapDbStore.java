package org.noear.folkmq.broker.watcher.mdb;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.noear.folkmq.broker.*;
import org.noear.folkmq.broker.watcher.utils.SnowflakeId;
import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.common.MqMetasResolver;
import org.noear.folkmq.common.MqUtils;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.EntityDefault;
import org.noear.socketd.transport.core.entity.MessageBuilder;
import org.noear.socketd.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author noear
 * @since 1.8
 */
public class MqMapDbStore extends MqStoreBase {
    protected static final Logger log = LoggerFactory.getLogger(MqMapDbStore.class);

    //服务端引用
    private MqBorkerInternal serverRef;
    private DB db;
    private HTreeMap<String, SubscribeDoc> subscribeDocColl;
    private HTreeMap<Long, MessageDoc> messageDocColl;

    //正在保持中
    private final AtomicBoolean inSaveProcess = new AtomicBoolean(false);
    //是否已启动
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public MqMapDbStore() {
        this(null);
    }

    public MqMapDbStore(String dataPath) {
        if (StrUtils.isEmpty(dataPath)) {
            dataPath = "data/mdb/";
        }

        File dataPath2 = new File(dataPath);
        if (!dataPath2.exists()) {
            dataPath2.mkdirs();
        }

        File filePath = new File(dataPath2, "folkmq.mdb");

        this.db = DBMaker.fileDB(filePath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .make();

        this.subscribeDocColl = db.hashMap(SubscribeDoc.class.getSimpleName())
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        this.messageDocColl = db.hashMap(MessageDoc.class.getSimpleName())
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
    }

    public boolean inSaveProcess() {
        return inSaveProcess.get();
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
            for (SubscribeDoc subs : subscribeDocColl.values()) {
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

    private boolean loadQueue1(String queueName) throws IOException {
        List<MessageDoc> msgList = messageDocColl.values().stream()
                .filter(m -> m.queueName.equals(queueName))
                .collect(Collectors.toList());
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

        subscribeDocColl.put(doc.queueName, doc);
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

        messageDocColl.put(doc.id, doc);

        messageHolder.setId(doc.id);
    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        if (isOk == false) {
            MessageDoc msg = messageDocColl.get(messageHolder.getId());
            if (msg != null) {
                msg.metaString = messageHolder.getEntity().metaString();
                messageDocColl.put(msg.id, msg);
            }
        }
    }

    @Override
    public void onRemove(String topic, String consumerGroup, MqMessageHolder messageHolder) {
        if (messageHolder.getId() > 0L) {
            messageDocColl.remove(messageHolder.getId());
        }
    }
}