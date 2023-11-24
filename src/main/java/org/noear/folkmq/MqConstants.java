package org.noear.folkmq;

/**
 * 常量
 *
 * @author noear
 * @since 1.0
 */
public interface MqConstants {
    /**
     * 消息事务Id
     */
    String MQ_TID = "mq.tid";
    /**
     * 消息主题
     */
    String MQ_TOPIC = "mq.topic";
    /**
     * 消息调度时间
     */
    String MQ_SCHEDULED = "mq.scheduled";

    /**
     * 消息者
     */
    String MQ_CONSUMER = "mq.consumer";
    /**
     * 派发次数
     */
    String MQ_TIMES = "mq.times";
    /**
     * 消费回执
     */
    String MQ_ACK = "mq.ack";

    /**
     * 指令：订阅
     */
    String MQ_CMD_SUBSCRIBE = "mq.cmd.subscribe";
    /**
     * 指令：发布
     */
    String MQ_CMD_PUBLISH = "mq.cmd.publish";
    /**
     * 指令：派发
     */
    String MQ_CMD_DISTRIBUTE = "mq.cmd.distribute";

    /**
     * 连接参数：ak
     */
    String PARAM_ACCESS_KEY = "ak";
    /**
     * 连接参数: sk
     */
    String PARAM_ACCESS_SECRET_KEY = "sk";
}
