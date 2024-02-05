package org.noear.folkmq.common;

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
     * 元信息：消息过期时间
     */
    String MQ_META_EXPIRATION = "mq.expiration";
    /**
     * 元信息：消息质量等级
     */
    String MQ_META_QOS = "mq.qos";
    /**
     * 元信息：消费者组
     */
    String MQ_META_CONSUMER_GROUP = "mq.consumer"; //此处不改动，算历史痕迹。保持向下兼容
    /**
     * 元信息：派发次数
     */
    String MQ_META_TIMES = "mq.times";
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
     * 事件：取消发布
     */
    String MQ_EVENT_UNPUBLISH = "mq.event.unpublish";
    /**
     * 事件：派发
     */
    String MQ_EVENT_DISTRIBUTE = "mq.event.distribute";
    /**
     * 事件：保存快照
     */
    String MQ_EVENT_SAVE = "mq.event.save";

    /**
     * 事件：加入集群
     * */
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
     * 经理人所有服务
     */
    String BROKER_AT_SERVER_ALL = "folkmq-server*";

    /**
     * 最大分片大小（1m）
     */
    int MAX_FRAGMENT_SIZE = 1024 * 1024;
}
