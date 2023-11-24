package org.noear.folkmq;

/**
 * 常量
 *
 * @author noear
 * @since 1.0
 */
public interface MqConstants {
    String MQ_TID = "mq.tid";
    String MQ_TOPIC = "mq.topic";
    String MQ_SCHEDULED = "mq.scheduled";

    String MQ_CONSUMER = "mq.consumer";
    String MQ_TIMES = "mq.times";
    String MQ_ACK = "mq.ack";

    String MQ_CMD_SUBSCRIBE = "mq.cmd.subscribe";
    String MQ_CMD_PUBLISH = "mq.cmd.publish";
    String MQ_CMD_DISTRIBUTE = "mq.cmd.distribute";

    String PARAM_ACCESS_KEY = "accessKey";
    String PARAM_ACCESS_SECRET_KEY = "accessSecretKey";
}
