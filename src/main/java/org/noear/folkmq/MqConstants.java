package org.noear.folkmq;

/**
 * 常量
 *
 * @author noear
 * @since 1.0
 */
public interface MqConstants {
    /**
     * 元信息：消息事务Id
     */
    String MQ_META_TID = "mq.tid";
    /**
     * 元信息：消息主题
     */
    String MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消息调度时间
     */
    String MQ_META_SCHEDULED = "mq.scheduled";
    /**
     * 元信息：消息质量等级
     */
    String MQ_META_QOS = "mq.qos";
    /**
     * 元信息：消息者
     */
    String MQ_META_CONSUMER = "mq.consumer";
    /**
     * 元信息：派发次数
     */
    String MQ_META_TIMES = "mq.times";
    /**
     * 元信息：消费回执
     */
    String MQ_META_ACK = "mq.ack";

    /**
     * 事件：订阅
     */
    String MQ_EVENT_SUBSCRIBE = "mq.event.subscribe";
    /**
     * 事件：发布
     */
    String MQ_EVENT_PUBLISH = "mq.event.publish";
    /**
     * 事件：派发
     */
    String MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：保存
     */
    String MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 连接参数：ak
     */
    String PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    String PARAM_ACCESS_SECRET_KEY = "sk";
}
