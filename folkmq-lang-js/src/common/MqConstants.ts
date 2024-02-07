
export class MqConstants {
    /**
     * 元信息：消息事务Id
     */
    static readonly MQ_META_TID: string = "mq.tid";
    /**
     * 元信息：消息主题
     */
    static readonly MQ_META_TOPIC: string = "mq.topic";
    /**
     * 元信息：消息调度时间
     */
    static readonly MQ_META_SCHEDULED: string = "mq.scheduled";
    /**
     * 元信息：消息过期时间
     */
    static readonly MQ_META_EXPIRATION: string = "mq.expiration";
    /**
     * 元信息：消息质量等级
     */
    static readonly MQ_META_QOS: string = "mq.qos";
    /**
     * 元信息：消费者组
     */
    static readonly MQ_META_CONSUMER_GROUP: string = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容
    /**
     * 元信息：派发次数
     */
    static readonly MQ_META_TIMES: string = "mq.times";
    /**
     * 元信息：消费回执
     */
    static readonly MQ_META_ACK: string = "mq.ack";
    /**
     * 元信息：执行确认
     */
    static readonly MQ_META_CONFIRM: string = "mq.confirm";
    /**
     * 元信息：批量处理
     */
    static readonly MQ_META_BATCH: string = "mq.batch";

    /**
     * 事件：订阅
     */
    static readonly MQ_EVENT_SUBSCRIBE: string = "mq.event.subscribe";
    /**
     * 事件：取消订阅
     */
    static readonly MQ_EVENT_UNSUBSCRIBE: string = "mq.event.unsubscribe";
    /**
     * 事件：发布
     */
    static readonly MQ_EVENT_PUBLISH: string = "mq.event.publish";
    /**
     * 事件：取消发布
     */
    static readonly MQ_EVENT_UNPUBLISH: string = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    static readonly MQ_EVENT_DISTRIBUTE: string = "mq.event.distribute";
    /**
     * 事件：保存快照
     */
    static readonly MQ_EVENT_SAVE: string = "mq.event.save";

    /**
     * 事件：加入集群
     * */
    static readonly MQ_EVENT_JOIN: string = "mq.event.join";

    /**
     * 事件：接口
     */
    static readonly MQ_API: string  = "mq.api";

    /**
     * 接口名
     */
    static readonly API_NAME: string  = "api.name";

    /**
     * 接口访问令牌
     */
    static readonly API_TOKEN: string  = "api.token";

    /**
     * 管理指令
     */
    static readonly ADMIN_PREFIX: string  = "admin.";

    /**
     * 管理视图-队列
     */
    static readonly ADMIN_VIEW_QUEUE: string = "admin.view.queue";

    /**
     * 管理队列-强制删除
     */
    static readonly ADMIN_QUEUE_FORCE_DELETE: string = "admin.queue.force.delete";
    /**
     * 管理队列-强制删除
     */
    static readonly ADMIN_QUEUE_FORCE_CLEAR: string = "admin.queue.force.clear";
    /**
     * 管理队列-强制派发
     */
    static readonly ADMIN_QUEUE_FORCE_DISTRIBUTE: string = "admin.queue.force.distribute";

    /**
     * 连接参数：ak
     */
    static readonly PARAM_ACCESS_KEY: string = "ak";
    /**
     * 连接参数: sk
     */
    static readonly PARAM_ACCESS_SECRET_KEY: string = "sk";

    /**
     * 主题与消息者间隔符
     */
    static readonly SEPARATOR_TOPIC_CONSUMER_GROUP: string = "#";

    /**
     * 经理人服务
     */
    static readonly BROKER_AT_SERVER: string = "folkmq-server";

    /**
     * 经理人服务
     */
    static readonly BROKER_AT_SERVER_HASH: string = "folkmq-server!";

    /**
     * 经理人所有服务
     */
    static readonly BROKER_AT_SERVER_ALL: string = "folkmq-server*";

    /**
     * 最大分片大小（1m）
     */
    static readonly MAX_FRAGMENT_SIZE: number = 1024 * 1024;
}
