package org.noear.folkmq.client;

import org.noear.socketd.transport.core.entity.StringEntity;

/**
 * 告警实体
 *
 * @author noear
 * @since 1.2
 */
public class MqAlarm extends StringEntity {
    public MqAlarm(String data){
        super(data);
    }
}
