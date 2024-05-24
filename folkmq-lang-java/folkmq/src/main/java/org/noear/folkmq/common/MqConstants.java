package org.noear.folkmq.common;

/**
 * 常量
 *
 * @author noear
 * @since 1.0
 */
public interface MqConstants {
    /**
     * 版本号
     */
    String FOLKMQ_VERSION = "folkmq-version";
    /**
     * 命名空间
     */
    String FOLKMQ_NAMESPACE = "folkmq-namespace";

    /**
     * 元信息：消息主键
     */
    String MQ_META_KEY = "mq.tid";

    /**
     * 元信息：消息主题
     */
    String MQ_META_TOPIC = "mq.topic";
    /**
     * 元信息：消费者组
     */
    String MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容

    /**
     * 元信息：消费回执
     */
    String MQ_META_ACK = "mq.ack";
    /**
     * 元信息：执行确认
     */
    String MQ_META_CONFIRM = "mq.confirm";
    /**
     * 元信息：批量处理
     */
    String MQ_META_BATCH = "mq.batch";
    /**
     * 元信息：执行回滚
     */
    String MQ_META_ROLLBACK = "mq.rollback";
    /**
     * 事件：订阅
     */
    String MQ_EVENT_SUBSCRIBE = "mq.event.subscribe";
    /**
     * 事件：取消订阅
     */
    String MQ_EVENT_UNSUBSCRIBE = "mq.event.unsubscribe";
    /**
     * 事件：发布
     */
    String MQ_EVENT_PUBLISH = "mq.event.publish";

    /**
     * 事件：发布二次提交
     */
    String MQ_EVENT_PUBLISH2 = "mq.event.publish2";

    /**
     * 事件：取消发布
     */
    String MQ_EVENT_UNPUBLISH = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    String MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：请求
     */
    String MQ_EVENT_REQUEST = "mq.event.request";
    /**
     * 事件：保存快照
     */
    String MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 事件：加入集群
     */
    String MQ_EVENT_JOIN = "mq.event.join";


    /**
     * 事件：接口
     */
    String MQ_API = "mq.api";

    /**
     * 接口名
     */
    String API_NAME = "api.name";

    /**
     * 接口访问令牌
     */
    String API_TOKEN = "api.token";

    /**
     * 管理指令
     */
    String ADMIN_PREFIX = "admin.";

    /**
     * 管理视图-队列
     */
    String ADMIN_VIEW_QUEUE = "admin.view.queue";

    /**
     * 管理队列-强制删除
     */
    String ADMIN_QUEUE_FORCE_DELETE = "admin.queue.force.delete";
    /**
     * 管理队列-强制删除
     */
    String ADMIN_QUEUE_FORCE_CLEAR = "admin.queue.force.clear";
    /**
     * 管理队列-强制派发
     */
    String ADMIN_QUEUE_FORCE_DISTRIBUTE = "admin.queue.force.distribute";

    /**
     * 连接参数：ak
     */
    String PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    String PARAM_ACCESS_SECRET_KEY = "sk";

    /**
     * 主题与消息者间隔符
     */
    String SEPARATOR_TOPIC_CONSUMER_GROUP = "#";

    /**
     * 经理人服务
     */
    String BROKER_AT_SERVER = "folkmq-server";

    /**
     * 经理人服务
     */
    String BROKER_AT_SERVER_HASH = "folkmq-server!";

    /**
     * 经理人所有服务
     */
    String BROKER_AT_SERVER_ALL = "folkmq-server*";

    /**
     * 事件缓存队列消息费者
     */
    String MQ_TRAN_CONSUMER_GROUP = "!";

    /**
     * 消息属性前缀
     */
    String MQ_ATTR_PREFIX = "!";

    /**
     * 客户端流超时默认值
     */
    long CLIENT_STREAM_TIMEOUT_DEFAULT = 30 * 1000;

    /**
     * 客户端写信号量默认值
     */
    int CLIENT_WRITE_SEMAPHORE_DEFAULT = 100;

    /**
     * 服务端流超时默认值
     */
    long SERVER_STREAM_TIMEOUT_DEFAULT = 60 * 1000 * 5;
}
