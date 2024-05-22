package org.noear.folkmq.common;

/**
 * 消息元信息 v2
 *
 * @author noear
 * @since 1.2
 */
public interface MqMetasV2 {
    /**
     * 元信息：消息主键
     */
    String MQ_META_KEY = "t0";

    /**
     * 元信息：消息元信息版本id
     */
    String MQ_META_VID = "v0";

    /**
     * 元信息：发送人
     */
    String MQ_META_SENDER = "s0";


    /**
     * 元信息：消息主题
     */
    String MQ_META_TOPIC = "t1";

    /**
     * 元信息：消费者组
     */
    String MQ_META_CONSUMER_GROUP = "c1"; //此处不改动，算历史痕迹。保持向下兼容


    /**
     * 元信息：消息调度时间
     */
    String MQ_META_SCHEDULED = "s1";
    /**
     * 元信息：消息过期时间
     */
    String MQ_META_EXPIRATION = "e1";
    /**
     * 元信息：消息是否有序
     */
    String MQ_META_SEQUENCE = "s2";
    /**
     * 元信息：消息是否事务
     */
    String MQ_META_TRANSACTION = "t4";

    /**
     * 元信息：消息是否广播
     */
    String MQ_META_BROADCAST = "b0";

    /**
     * 元信息：消息质量等级
     */
    String MQ_META_QOS = "q1";
    /**
     * 元信息：派发次数
     */
    String MQ_META_TIMES = "t2";
    /**
     * 元信息：标签
     */
    String MQ_META_TAG = "t5";
}