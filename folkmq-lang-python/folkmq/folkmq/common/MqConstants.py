class MqConstants:
    #版本号
    FOLKMQ_VERSION = "folkmq-version"

    #命名空间
    FOLKMQ_NAMESPACE = "folkmq-namespace"

    #元信息：消息主建
    MQ_META_KEY = "mq.tid"

    #元信息：消息主题
    MQ_META_TOPIC = "mq.topic"
    #元信息：消费者组
    MQ_META_CONSUMER_GROUP = "mq.consumer" # 此处不改动，算历史痕迹。保持向下兼容

    #元信息：消费回执
    MQ_META_ACK = "mq.ack"
    #元信息：执行确认
    MQ_META_CONFIRM = "mq.confirm"
    #元信息：批量处理
    MQ_META_BATCH = "mq.batch"
    #元信息：执行回滚
    MQ_META_ROLLBACK = "mq.rollback"
    #事件：订阅
    MQ_EVENT_SUBSCRIBE = "mq.event.subscribe"
    #事件：取消订阅
    MQ_EVENT_UNSUBSCRIBE = "mq.event.unsubscribe"
    #事件：发布
    MQ_EVENT_PUBLISH = "mq.event.publish"

    #事件：发布二次提交
    MQ_EVENT_PUBLISH2 = "mq.event.publish2"

    #事件：取消发布
    MQ_EVENT_UNPUBLISH = "mq.event.unpublish"
    #事件：派发
    MQ_EVENT_DISTRIBUTE = "mq.event.distribute"
    #事件：请求
    MQ_EVENT_REQUEST = "mq.event.request"
    #事件：保存快照
    MQ_EVENT_SAVE = "mq.event.save"

    #事件：加入集群
    MQ_EVENT_JOIN = "mq.event.join"

    #事件：接口
    MQ_API = "mq.api"

    #接口名
    API_NAME = "api.name"

    #接口访问令牌
    API_TOKEN = "api.token"

    #管理指令
    ADMIN_PREFIX = "admin."

    #管理视图 - 队列
    ADMIN_VIEW_QUEUE = "admin.view.queue"

    #管理队列 - 强制删除
    ADMIN_QUEUE_FORCE_DELETE = "admin.queue.force.delete"
    #管理队列 - 强制删除
    ADMIN_QUEUE_FORCE_CLEAR = "admin.queue.force.clear"
    #管理队列 - 强制派发
    ADMIN_QUEUE_FORCE_DISTRIBUTE = "admin.queue.force.distribute"

    #连接参数：ak
    PARAM_ACCESS_KEY = "ak"
    #连接参数: sk
    PARAM_ACCESS_SECRET_KEY = "sk"

    #主题与消息者间隔符
    SEPARATOR_TOPIC_CONSUMER_GROUP = "#"

    #经理人服务
    PROXY_AT_BROKER = "folkmq-server"

    #经理人服务
    PROXY_AT_BROKER_HASH = "folkmq-server!"

    #经理人所有服务
    PROXY_AT_BROKER_ALL = "folkmq-server*"

    #事件缓存队列消息费者
    MQ_TRAN_CONSUMER_GROUP = "!"

    #事件缓存队列消息费者
    MQ_ATTR_PREFIX = "!"

    #最大分片大小（1m）
    MAX_FRAGMENT_SIZE = 1024 * 1024