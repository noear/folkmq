package org.noear.folkmq.common;

/**
 * 消息元信息 v1
 *
 * @author noear
 * @since 1.1
 */
public interface MqMetasV1 {
    /**
     * 元信息：消息事务Id
     */
    String MQ_META_TID = "mq.tid";

    /**
     * 元信息：消息主题
     */
    String MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消费者组
     */
    String MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容

    /**
     * 元信息：消息调度时间
     */
    String MQ_META_SCHEDULED = "mq.scheduled";
    /**
     * 元信息：消息过期时间
     */
    String MQ_META_EXPIRATION = "mq.expiration";
    /**
     * 元信息：消息是否有序
     */
    String MQ_META_SEQUENCE = "mq.sequence";

    /**
     * 元信息：消息质量等级
     */
    String MQ_META_QOS = "mq.qos";
    /**
     * 元信息：派发次数
     */
    String MQ_META_TIMES = "mq.times";
}