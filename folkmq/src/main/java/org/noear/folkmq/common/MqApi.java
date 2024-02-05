package org.noear.folkmq.common;

/**
 * @author noear
 * @since 1.0
 */
public interface MqApi {
    String MQ_QUEUE_LIST = "mq.queue.list";
    String MQ_QUEUE_VIEW_MESSAGE = "mq.queue.view.message";
    String MQ_QUEUE_VIEW_SESSION = "mq.queue.view.session";
    String MQ_QUEUE_FORCE_CLEAR = "mq.queue.force.clear";
    String MQ_QUEUE_FORCE_DELETE = "mq.queue.force.delete";
    String MQ_QUEUE_FORCE_DISTRIBUTE = "mq.queue.force.distribute";
}
