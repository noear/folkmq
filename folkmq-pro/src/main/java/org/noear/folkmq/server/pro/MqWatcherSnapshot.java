package org.noear.folkmq.server.pro;

import org.noear.folkmq.common.MqConstants;
import org.noear.folkmq.server.*;
import org.noear.folkmq.server.pro.utils.IoUtils;
import org.noear.snack.ONode;
import org.noear.snack.core.Feature;
import org.noear.snack.core.Options;
import org.noear.socketd.transport.core.Entity;
import org.noear.socketd.transport.core.Flags;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.internal.MessageDefault;
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
    private final AtomicBoolean inSaveProcess = new AtomicBoolean(false);
    //是否已启动
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

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
    }

    public boolean inSaveProcess() {
        return inSaveProcess.get();
    }

    @Override
    public void init(MqServiceInternal serverInternal) {
        this.serverRef = serverInternal;
    }

    @Override
    public void onStartBefore() {
        isStarted.set(false);
        loadSubscribeMap();
    }

    @Override
    public void onStartAfter() {
        RunUtils.asyncAndTry(() -> {
            try {
                loadQueue();
            } finally {
                isStarted.set(true);
            }
        });
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

            ONode subscribeMapJson = ONode.loadStr(subscribeMapJsonStr, Feature.DisThreadLocal);
            for (String topic : subscribeMapJson.obj().keySet()) {
                ONode oQueueNameList = subscribeMapJson.get(topic);
                for (ONode oQueueName : oQueueNameList.ary()) {
                    String consumerGroup = oQueueName.getString().split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP)[1];
                    serverRef.subscribeDo(topic, consumerGroup, null);
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
        String queueFileName = queueName.replace(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP, "/") + file_suffix;
        File queueFile = new File(directory, queueFileName);
        if (queueFile.exists() == false) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(queueFile))) {
            while (true) {
                //一行行读取（避免大 json 坏掉后，全坏了）//也比较省内存
                String messageJsonStr = reader.readLine();
                if (messageJsonStr == null) {
                    break;
                }

                if (messageJsonStr.length() > 0 && messageJsonStr.endsWith("}")) {
                    ONode messageJson = ONode.loadStr(messageJsonStr, Feature.DisThreadLocal);

                    String metaString = messageJson.get("meta").getString();
                    String data = messageJson.get("data").getString();

                    if(data == null){
                        //可能会有异常，造成数据不完整
                        continue;
                    }

                    Entity entity = new StringEntity(data).metaString(metaString);
                    Message message = new MessageDefault()
                            .sid(Utils.guid())
                            .flag(Flags.Message)
                            .entity(entity);


                    String tid = message.meta(MqConstants.MQ_META_TID);
                    int qos = "0".equals(message.meta(MqConstants.MQ_META_QOS)) ? 0 : 1;
                    int times = Integer.parseInt(message.metaOrDefault(MqConstants.MQ_META_TIMES, "0"));
                    long scheduled = 0;
                    String scheduledStr = message.meta(MqConstants.MQ_META_SCHEDULED);
                    if (Utils.isNotEmpty(scheduledStr)) {
                        scheduled = Long.parseLong(scheduledStr);
                    }

                    serverRef.exchangeDo(queueName, message, tid, qos, times, scheduled);
                }
            }

        }

        return true;
    }


    //////////////////////////////////////////

    @Override
    public void onStopAfter() {
        onSave();
        isStarted.set(false);
    }

    @Override
    public void onSave() {
        if (isStarted.get() == false) {
            //未加载完成（不可保存，否则会盖掉加载中的数据）
            return;
        }

        if (inSaveProcess.get()) {
            return;
        } else {
            inSaveProcess.set(true);
        }

        try {
            saveSubscribeMap();
            saveQueue();
        } finally {
            inSaveProcess.set(false);
        }
    }

    /**
     * 保存订阅关系（确保线程安全）
     */
    private void saveSubscribeMap() {
        Map<String, Set<String>> subscribeMap = serverRef.getSubscribeMap();
        if (subscribeMap.size() == 0) {
            return;
        }

        ONode subscribeMapJson = new ONode(Options.def().add(Feature.PrettyFormat, Feature.DisThreadLocal)).asObject();
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
    private void saveQueue() {
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

        Map<String, MqQueue> queueMap = serverRef.getQueueMap();

        for (String queueName : queueNameSet) {
            MqQueue queue = queueMap.get(queueName);

            try {
                saveQueue1(queueName, (MqQueueDefault) queue);

                log.info("Server persistent messageQueue completed, queueName={}", queueName);
            } catch (Exception e) {
                log.warn("Server persistent messageQueue failed, queueName={}", queueName, e);
            }
        }

        log.info("Server persistent saveQueue completed");
    }

    private void saveQueue1(String queueName, MqQueueDefault queue) throws IOException {
        String[] topicConsumerGroupAry = queueName.split(MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP);
        File topicDir = new File(directory, topicConsumerGroupAry[0]);
        if (topicDir.exists() == false) {
            topicDir.mkdirs();
        }

        String queueFileName = topicConsumerGroupAry[0] + "/" + topicConsumerGroupAry[1] + file_suffix;
        File queueFile = new File(directory, queueFileName);
        if (queueFile.exists() == false) {
            queueFile.createNewFile();
        }


        if (queue != null) {
            List<MqMessageHolder> messageList = new ArrayList<>(queue.getMessageMap().values());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(queueFile))) {
                for (MqMessageHolder messageHolder : messageList) {
                    if (messageHolder.isDone()) {
                        continue;
                    }

                    try {
                        Entity entity = messageHolder.getContent();
                        ONode entityJson = new ONode(Options.def().add(Feature.DisThreadLocal));
                        entityJson.set("meta", entity.metaString());
                        entityJson.set("data", entity.dataAsString());

                        //一条写一行（大 json 容易坏掉）//也比较省内存
                        writer.write(entityJson.toJson());
                        writer.newLine();
                    } catch (Exception e) {
                        log.warn("Server persistent message failed, tid={}", messageHolder.getTid(), e);
                    }
                }
            }
        } else {
            saveSnapshotFile(queueFile, "");
        }
    }

    /**
     * 读取快照文件
     */
    private static String readSnapshotFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] bytes = IoUtils.transferToBytes(input);

            //解压
            byte[] contentBytes = bytes;//GzipUtils.decompress(bytes);
            return new String(contentBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存快照文件
     */
    private static void saveSnapshotFile(File file, String content) throws IOException {
        //压缩
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = contentBytes;//GzipUtils.compress(contentBytes);

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             OutputStream out = new FileOutputStream(file)) {
            IoUtils.transferTo(input, out);
        }
    }
}