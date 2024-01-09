
export class MqConstants{
    /**
     * 元信息：消息事务Id
     */
    static MQ_META_TID = "mq.tid";
    /**
     * 元信息：消息主题
     */
    static MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消息调度时间
     */
    static MQ_META_SCHEDULED = "mq.scheduled";
    /**
     * 元信息：消息质量等级
     */
    static MQ_META_QOS = "mq.qos";
    /**
     * 元信息：消费者组
     */
    static MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容
    /**
     * 元信息：派发次数
     */
    static MQ_META_TIMES = "mq.times";
    /**
     * 元信息：消费回执
     */
    static MQ_META_ACK = "mq.ack";
    /**
     * 元信息：执行确认
     */
    static MQ_META_CONFIRM = "mq.confirm";
    /**
     * 元信息：批量处理
     */
    static MQ_META_BATCH = "mq.batch";

    /**
     * 事件：订阅
     */
    static MQ_EVENT_SUBSCRIBE = "mq.event.subscribe";
    /**
     * 事件：取消订阅
     */
    static MQ_EVENT_UNSUBSCRIBE = "mq.event.unsubscribe";
    /**
     * 事件：发布
     */
    static MQ_EVENT_PUBLISH = "mq.event.publish";
    /**
     * 事件：取消发布
     */
    static MQ_EVENT_UNPUBLISH = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    static MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：保存快照
     */
    static MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 事件：加入集群
     * */
    static MQ_EVENT_JOIN = "mq.event.join";

    /**
     * 管理视图-队列
     */
    static ADMIN_VIEW_QUEUE = "admin.view.queue";

    /**
     * 连接参数：ak
     */
    static PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    static PARAM_ACCESS_SECRET_KEY = "sk";

    /**
     * 主题与消息者间隔符
     */
    static SEPARATOR_TOPIC_CONSUMER_GROUP = "#";

    /**
     * 经理人服务
     */
    static BROKER_AT_SERVER = "folkmq-server";

    /**
     * 经理人所有服务
     */
    static BROKER_AT_SERVER_ALL = "folkmq-server*";

    /**
     * 最大分片大小（1m）
     */
    static MAX_FRAGMENT_SIZE = 1024 * 1024;
}
