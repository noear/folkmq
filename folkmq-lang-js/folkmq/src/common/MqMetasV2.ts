/**
 * 消息元信息 v2
 *
 * @author noear
 * @since 1.2
 */
export class MqMetasV2{
    /**
     * 元信息：消息跟踪Id
     */
    static readonly MQ_META_TID = "t0";
    /**
     * 元信息：消息元信息版本id
     */
    static readonly MQ_META_VID = "v0";

    /**
     * 元信息：发送人
     */
    static readonly MQ_META_SENDER = "s0";


    /**
     * 元信息：消息主题
     */
    static readonly MQ_META_TOPIC = "t1";

    /**
     * 元信息：消费者组
     */
    static readonly MQ_META_CONSUMER_GROUP = "c1"; //此处不改动，算历史痕迹。保持向下兼容


    /**
     * 元信息：消息调度时间
     */
    static readonly MQ_META_SCHEDULED = "s1";
    /**
     * 元信息：消息过期时间
     */
    static readonly MQ_META_EXPIRATION = "e1";
    /**
     * 元信息：消息是否有序
     */
    static readonly MQ_META_SEQUENCE = "s2";
    /**
     * 元信息：消息是否事务
     */
    static readonly MQ_META_TRANSACTION = "t4";

    /**
     * 元信息：消息质量等级
     */
    static readonly MQ_META_QOS = "q1";
    /**
     * 元信息：派发次数
     */
    static readonly MQ_META_TIMES = "t2";
    /**
     * 元信息：标签
     */
    static readonly MQ_META_TAG = "t5";
}