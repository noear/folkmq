package org.noear.folkmq.broker.watcher.ldb;

import com.github.artbits.quickio.api.JDB;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.noear.folkmq.broker.*;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author noear 2025/1/2 created
 */
public class MqWatcherQuickIo implements MqWatcher {
    protected static final Logger log = LoggerFactory.getLogger(MqWatcherQuickIo.class);

    //服务端引用
    private MqBorkerInternal serverRef;
    private JDB db;
    private com.github.artbits.quickio.api.Collection<SubscribeDoc> subscribeDocColl;
    private com.github.artbits.quickio.api.Collection<MessageDoc> messageDocColl;

    //正在保持中
    private final AtomicBoolean inSaveProcess = new AtomicBoolean(false);
    //是否已启动
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public MqWatcherQuickIo() {
        this(null);
    }

    public MqWatcherQuickIo(String dataPath) {
        if (StrUtils.isEmpty(dataPath)) {
            dataPath = "data/ldb/";
        }

        String dataPath2 = dataPath;

        this.db = QuickIO.db(Config.of(c -> c.path(dataPath2).name("folkmq")));

        this.subscribeDocColl = db.collection(SubscribeDoc.class);
        this.messageDocColl = db.collection(MessageDoc.class);
    }

    public boolean inSaveProcess() {
        return inSaveProcess.get();
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

    @Override
    public void onStartAfter() {

    }

    @Override
    public void onStopBefore() {

    }

    /**
     * 加载订阅关系（确保线程安全）
     */
    private void loadSubscribeMap() {
        try {
            for (SubscribeDoc subs : subscribeDocColl.findAll()) {
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
        List<MessageDoc> msgList = messageDocColl.find(m -> m.queueName.equals(queueName));
        for (MessageDoc msg : msgList) {
            if (msg.data == null) {
                continue;
            }

            EntityDefault entity = new EntityDefault();
            if (msg.ver < 2) {
                //旧版用 string
                entity.dataSet(msg.data.getBytes(StandardCharsets.UTF_8));
            } else {
                //新版用 base64 支持二进制
                entity.dataSet(Base64.getDecoder().decode(msg.data));
            }
            entity.metaStringSet(msg.metaString);
            Message message = new MessageBuilder()
                    .sid(StrUtils.guid())
                    .flag(Flags.Message)
                    .entity(entity)
                    .build();

            MqMetasResolver mr = MqUtils.getOf(message);
            MqDraft draft = new MqDraft(mr, message);

            MqQueue queue = serverRef.getQueue(queueName);
            serverRef.routingToQueueDo(draft, queue, msg.objectId());
        }

        return true;
    }


    /// ///////////////////////////////////////

    @Override
    public void onStopAfter() {
        isStarted.set(false);
    }

    @Override
    public void onSave() {

    }

    @Override
    public void onSubscribe(String topic, String consumerGroup, Session session) {
        SubscribeDoc doc = new SubscribeDoc();
        doc.topic = topic;
        doc.queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;

        subscribeDocColl.save(doc);
    }

    @Override
    public void onUnSubscribe(String topic, String consumerGroup, Session session) {

    }

    @Override
    public void onPublish(Message message) {

    }

    @Override
    public void onUnPublish(Message message) {

    }

    @Override
    public void onRouting(MqMessageHolder messageHolder) {
        if (messageHolder.getId() > 0L) {
            return;
        }

        MessageDoc doc = new MessageDoc();
        doc.ver = 2;
        doc.queueName = messageHolder.getQueueName();
        doc.metaString = messageHolder.getEntity().metaString();
        doc.data = messageHolder.getEntity().dataAsString();

        messageDocColl.save(doc);

        messageHolder.setId(doc.objectId());
    }

    @Override
    public void onDistribute(String topic, String consumerGroup, MqMessageHolder messageHolder) {

    }

    @Override
    public void onAcknowledge(String topic, String consumerGroup, MqMessageHolder messageHolder, boolean isOk) {
        if (isOk && messageHolder.getId() > 0L) {
            messageDocColl.delete(messageHolder.getId());
        }
    }
}
