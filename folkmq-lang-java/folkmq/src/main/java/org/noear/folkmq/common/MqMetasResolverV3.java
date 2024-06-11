package org.noear.folkmq.common;

/**
 * 消息元信息分析器 v2
 *
 * @author noear
 * @since 1.2
 */
public class MqMetasResolverV3 extends MqMetasResolverV2 {
    @Override
    public int version() {
        return 3;
    }
}