package org.noear.folkmq.server.pro;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.*;
import org.noear.snack.ONode;
import org.noear.snack.core.Feature;
import org.noear.snack.core.Options;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.internal.MessageDefault;
import org.noear.socketd.utils.GzipUtils;
import org.noear.socketd.utils.IoUtils;
import org.noear.socketd.utils.RunUtils;
import org.noear.socketd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息观察者 - 快照持久化（实现持久化）
 *
 * @author noear
 * @since 1.0
 */
public class MqWatcherSnapshot extends MqWatcherDefault {
    protected static final Logger log = LoggerFactory.getLogger(MqWatcherSnapshot.class);
    private static final String file_suffix = ".fdb";

    //服务端引用
    private MqServiceInternal serverRef;

    //文件目录
    private final File directory;

    //正在保持中
    private final AtomicBoolean inSaveProcess;

    public MqWatcherSnapshot() {
        this(null);
    }

    public MqWatcherSnapshot(String dataPath) {
        if (Utils.isEmpty(dataPath)) {
            dataPath = "./data/fdb/";
        }

        this.directory = new File(dataPath);

        if (this.directory.exists() == false) {
            this.directory.mkdirs();
        }

        this.inSaveProcess = new AtomicBoolean(false);
    }

    @Override
    public void init(MqServiceInternal serverInternal) {
        this.serverRef = serverInternal;
    }

    @Override
    public synchronized void onStartBefore() {
        loadSubscribeMap();
    }

    @Override
    public void onStartAfter() {
        RunUtils.asyncAndTry(this::loadTopicConsumerQueue);
    }

    /**
     * 加载订阅关系（确保线程安全）
     */
    private void loadSubscribeMap() {
        File subscribeMapFile = new File(directory, "subscribe-map.fdb");
        if (subscribeMapFile.exists() == false) {
            return;
        }

        try {
            String subscribeMapJsonStr = readSnapshotFile(subscribeMapFile);

            ONode subscribeMapJson = ONode.loadStr(subscribeMapJsonStr);
            for (String topic : subscribeMapJson.obj().keySet()) {
                ONode topicConsumerList = subscribeMapJson.get(topic);
                for (ONode topicConsumer : topicConsumerList.ary()) {
                    String consumer = topicConsumer.getString().split(MqConstants.SEPARATOR_TOPIC_CONSUMER)[1];
                    serverRef.subscribeDo(topic, consumer, null);
                }
            }

            log.info("Server persistent load subscribeMap completed");
        } catch (Exception e) {
            log.warn("Server persistent load subscribeMap failed", e);
        }
    }

    /**
     * 加载主题消费队列记录（确保线程安全）
     */
    private void loadTopicConsumerQueue() {
        Map<String, Set<String>> subscribeMap = serverRef.getSubscribeMap();
        if (subscribeMap.size() == 0) {
            return;
        }

        List<String> topicList = new ArrayList<>(subscribeMap.keySet());

        Set<String> topicConsumerSet = new HashSet<>();
        for (String topic : topicList) {
            Set<String> topicConsumerSetTmp = subscribeMap.get(topic);
            if (topicConsumerSetTmp != null) {
                topicConsumerSet.addAll(topicConsumerSetTmp);
            }
        }

        for (String topicConsumer : topicConsumerSet) {
            try {
                loadTopicConsumerQueue1(topicConsumer);

                log.info("Server persistent load messageQueue completed, topicConsumer={}", topicConsumer);
            } catch (Exception e) {
                log.warn("Server persistent load messageQueue failed, topicConsumer={}", topicConsumer, e);
            }
        }
    }

    private boolean loadTopicConsumerQueue1(String topicConsumer) throws IOException {
        String topicConsumerQueueFileName = topicConsumer.replace(MqConstants.SEPARATOR_TOPIC_CONSUMER, "/") + file_suffix;
        File topicConsumerQueueFile = new File(directory, topicConsumerQueueFileName);
        if (topicConsumerQueueFile.exists() == false) {
            return false;
        }

        String topicConsumerQueueJsonStr = readSnapshotFile(topicConsumerQueueFile);
        ONode topicConsumerQueueJson = ONode.loadStr(topicConsumerQueueJsonStr);

        for (ONode messageJson : topicConsumerQueueJson.ary()) {
            String metaString = messageJson.get("meta").getString();
            String data = messageJson.get("data").getString();

            Entity entity = new StringEntity(data).metaString(metaString);
            Message message = new MessageDefault()
                    .sid(Utils.guid())
                    .flag(Flags.Message)
                    .entity(entity);
            serverRef.exchangeDo(message);
        }

        return true;
    }


    //////////////////////////////////////////

    @Override
    public void onStopAfter() {
        onSave();
    }

    @Override
    public synchronized void onSave() {
        if (inSaveProcess.get()) {
            return;
        } else {
            inSaveProcess.set(true);
        }

        saveSubscribeMap();
        saveTopicConsumerQueue();

        inSaveProcess.set(false);
    }

    /**
     * 保存订阅关系（确保线程安全）
     */
    private void saveSubscribeMap() {
        Map<String, Set<String>> subscribeMap = serverRef.getSubscribeMap();
        if (subscribeMap.size() == 0) {
            return;
        }

        ONode subscribeMapJson = new ONode(Options.def().add(Feature.PrettyFormat)).asObject();
        List<String> topicList = new ArrayList<>(subscribeMap.keySet());
        for (String topic : topicList) {
            List<String> topicConsumerList = new ArrayList<>(subscribeMap.get(topic));
            subscribeMapJson.set(topic, topicConsumerList);
        }
        File subscribeMapFile = new File(directory, "subscribe-map.fdb");

        try {
            if (subscribeMapFile.exists() == false) {
                subscribeMapFile.createNewFile();
            }

            saveSnapshotFile(subscribeMapFile, subscribeMapJson.toJson());

            log.info("Server persistent saveSubscribeMap completed");
        } catch (Exception e) {
            log.warn("Server persistent saveSubscribeMap failed");
        }
    }

    /**
     * 保存主题消费队列记录（确保线程安全）
     */
    private void saveTopicConsumerQueue() {
        Map<String, Set<String>> subscribeMap = serverRef.getSubscribeMap();
        if (subscribeMap.size() == 0) {
            return;
        }

        List<String> topicList = new ArrayList<>(subscribeMap.keySet());

        Set<String> topicConsumerSet = new HashSet<>();
        for (String topic : topicList) {
            Set<String> topicConsumerSetTmp = subscribeMap.get(topic);
            if (topicConsumerSetTmp != null) {
                topicConsumerSet.addAll(topicConsumerSetTmp);
            }
        }

        Map<String, MqTopicConsumerQueue> topicConsumerMap = serverRef.getTopicConsumerMap();

        for (String topicConsumer : topicConsumerSet) {
            MqTopicConsumerQueue topicConsumerQueue = topicConsumerMap.get(topicConsumer);

            try {
                saveTopicConsumerQueue1(topicConsumer, topicConsumerQueue);

                log.info("Server persistent messageQueue completed, topicConsumer={}", topicConsumer);
            } catch (Exception e) {
                log.warn("Server persistent messageQueue failed, topicConsumer={}", topicConsumer, e);
            }
        }

        log.info("Server persistent saveTopicConsumerQueue completed");
    }

    private void saveTopicConsumerQueue1(String topicConsumer, MqTopicConsumerQueue topicConsumerQueue) throws IOException {
        ONode topicConsumerQueueJson = new ONode(Options.def().add(Feature.PrettyFormat)).asArray();

        if (topicConsumerQueue != null) {
            List<MqMessageHolder> messageList = new ArrayList<>(topicConsumerQueue.getMessageMap().values());
            for (MqMessageHolder messageHolder : messageList) {
                if (messageHolder.isDone()) {
                    continue;
                }

                try {
                    Entity entity = messageHolder.getContent();
                    ONode entityJson = topicConsumerQueueJson.addNew();
                    entityJson.set("meta", entity.metaString());
                    entityJson.set("data", entity.dataAsString());
                } catch (Exception e) {
                    log.warn("Server persistent message failed, tid={}", messageHolder.getTid(), e);
                }
            }
        }

        String[] topicConsumerAry = topicConsumer.split(MqConstants.SEPARATOR_TOPIC_CONSUMER);
        File topicConsumerQueueDir = new File(directory, topicConsumerAry[0]);
        if (topicConsumerQueueDir.exists() == false) {
            topicConsumerQueueDir.mkdirs();
        }

        String topicConsumerQueueFileName = topicConsumerAry[0] + "/" + topicConsumerAry[1] + file_suffix;
        File topicConsumerQueueFile = new File(directory, topicConsumerQueueFileName);
        if (topicConsumerQueueFile.exists() == false) {
            topicConsumerQueueFile.createNewFile();
        }

        saveSnapshotFile(topicConsumerQueueFile, topicConsumerQueueJson.toJson());
    }

    /**
     * 读取快照文件
     */
    private static String readSnapshotFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] bytes = IoUtils.transferToBytes(input);

            //解压
            byte[] contentBytes = GzipUtils.decompress(bytes);
            return new String(contentBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存快照文件
     */
    private static void saveSnapshotFile(File file, String content) throws IOException {
        //压缩
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = GzipUtils.compress(contentBytes);

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             OutputStream out = new FileOutputStream(file)) {
            IoUtils.transferTo(input, out);
        }
    }
}