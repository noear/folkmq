package org.noear.folkmq.client;

import org.noear.socketd.utils.StrUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noear
 * @since 1.1
 */
public class MqTransactionImpl implements MqTransaction {
    private final MqClientInternal client;
    private final List<String> tidAry;
    private final String tmid;

    public MqTransactionImpl(MqClientInternal client) {
        this.client = client;
        this.tidAry = new ArrayList<>();
        this.tmid = StrUtils.guid();
    }


    /**
     * 事务开始
     */
    public void binding(MqMessage message) {
        tidAry.add(message.getTid());
        message.internalSender(client.name());
    }


    @Override
    public String tmid() {
        return tmid;
    }


    /**
     * 事务提交
     */
    @Override
    public void commit() throws IOException {
        client.publish2(tmid, tidAry, false);
    }

    /**
     * 事务回滚
     */
    @Override
    public void rollback() throws IOException {
        client.publish2(tmid, tidAry, true);
    }
}
