
export class MqConstants {
    /**
     * 版本号
     */
    static readonly FOLKMQ_VERSION: string = "folkmq-version";
    /**
     * 命名空间
     */
    static readonly FOLKMQ_NAMESPACE: string = "folkmq-namespace";

    /**
     * 元信息：消息主建
     */
    static readonly MQ_META_KEY = "mq.tid";

    /**
     * 元信息：消息主题
     */
    static readonly MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消费者组
     */
    static readonly MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容

    /**
     * 元信息：消费回执
     */
    static readonly MQ_META_ACK = "mq.ack";
    /**
     * 元信息：执行确认
     */
    static readonly MQ_META_CONFIRM = "mq.confirm";
    /**
     * 元信息：批量处理
     */
    static readonly MQ_META_BATCH = "mq.batch";
    /**
     * 元信息：执行回滚
     */
    static readonly MQ_META_ROLLBACK = "mq.rollback";
    /**
     * 事件：订阅
     */
    static readonly MQ_EVENT_SUBSCRIBE = "mq.event.subscribe";
    /**
     * 事件：取消订阅
     */
    static readonly MQ_EVENT_UNSUBSCRIBE = "mq.event.unsubscribe";
    /**
     * 事件：发布
     */
    static readonly MQ_EVENT_PUBLISH = "mq.event.publish";

    /**
     * 事件：发布二次提交
     */
    static readonly MQ_EVENT_PUBLISH2 = "mq.event.publish2";

    /**
     * 事件：取消发布
     */
    static readonly MQ_EVENT_UNPUBLISH = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    static readonly MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：请求
     * */
    static readonly MQ_EVENT_REQUEST = "mq.event.request";
    /**
     * 事件：保存快照
     */
    static readonly MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 事件：加入集群
     * */
    static readonly MQ_EVENT_JOIN = "mq.event.join";


    /**
     * 事件：接口
     */
    static readonly MQ_API = "mq.api";

    /**
     * 接口名
     */
    static readonly API_NAME = "api.name";

    /**
     * 接口访问令牌
     */
    static readonly API_TOKEN = "api.token";

    /**
     * 管理指令
     */
    static readonly ADMIN_PREFIX = "admin.";

    /**
     * 管理视图-队列
     */
    static readonly ADMIN_VIEW_QUEUE = "admin.view.queue";

    /**
     * 管理队列-强制删除
     */
    static readonly ADMIN_QUEUE_FORCE_DELETE = "admin.queue.force.delete";
    /**
     * 管理队列-强制删除
     */
    static readonly ADMIN_QUEUE_FORCE_CLEAR = "admin.queue.force.clear";
    /**
     * 管理队列-强制派发
     */
    static readonly ADMIN_QUEUE_FORCE_DISTRIBUTE = "admin.queue.force.distribute";

    /**
     * 连接参数：ak
     */
    static readonly PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    static readonly PARAM_ACCESS_SECRET_KEY = "sk";

    /**
     * 主题与消息者间隔符
     */
    static readonly SEPARATOR_TOPIC_CONSUMER_GROUP = "#";

    /**
     * 经理人服务
     */
    static readonly PROXY_AT_BROKER = "folkmq-server";

    /**
     * 经理人服务
     */
    static readonly PROXY_AT_BROKER_HASH = "folkmq-server!";

    /**
     * 经理人所有服务
     */
    static readonly PROXY_AT_BROKER_ALL = "folkmq-server*";

    /**
     * 事件缓存队列消息费者
     * */
    static readonly MQ_TRAN_CONSUMER_GROUP = "!";

    /**
     * 事件缓存队列消息费者
     * */
    static readonly MQ_ATTR_PREFIX = "!";

    /**
     * 最大分片大小（1m）
     */
    static readonly MAX_FRAGMENT_SIZE = 1024 * 1024;
}
