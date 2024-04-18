package org.noear.folkmq.client;

import org.noear.socketd.utils.StrUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 事务实现
 *
 * @author noear
 * @since 1.2
 */
public class MqTransactionImpl implements MqTransaction {
    private final MqClientInternal client;
    private final List<String> keyAry;
    private final String tmid;

    public MqTransactionImpl(MqClientInternal client) {
        this.client = client;
        this.keyAry = new ArrayList<>();
        this.tmid = StrUtils.guid();
    }

    @Override
    public String tmid() {
        return tmid;
    }

    /**
     * 事务绑定
     */
    @Override
    public void binding(MqMessage message) {
        keyAry.add(message.getKey());
        message.internalSender(client.name());
    }

    /**
     * 事务提交
     */
    @Override
    public void commit() throws IOException {
        client.publish2(tmid, keyAry, false);
    }

    /**
     * 事务回滚
     */
    @Override
    public void rollback() throws IOException {
        client.publish2(tmid, keyAry, true);
    }
}
