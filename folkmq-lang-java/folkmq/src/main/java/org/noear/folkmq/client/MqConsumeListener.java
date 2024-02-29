package org.noear.folkmq.client;

/**
 * 消费监听器（方便给DI容器区分类型）
 *
 * @author noear
 * @since 1.0
 */
@FunctionalInterface
public interface MqConsumeListener extends MqConsumeHandler {

}
