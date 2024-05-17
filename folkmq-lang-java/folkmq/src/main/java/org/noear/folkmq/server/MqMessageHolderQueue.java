package org.noear.folkmq.server;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * MqMessageHolder 数据队列
 *
 * @author noear
 * @since 1.4
 */
public class MqMessageHolderQueue extends DelayQueue<MqMessageHolder> {
    //消息计数器
    private final LongAdder[] _counters = new LongAdder[9];

    public MqMessageHolderQueue() {
        super();

        //初始化计数器
        for (int i = 0; i < _counters.length; i++) {
            _counters[i] = new LongAdder();
        }
    }

    @Override
    public boolean add(MqMessageHolder mh) {
        countAdd(mh);
        return super.add(mh);
    }

    @Override
    public MqMessageHolder poll() {
        MqMessageHolder mh = super.poll();
        if (mh != null) {
            countSub(mh);
        }
        return mh;
    }

    @Override
    public boolean remove(Object o) {
        boolean tmp = super.remove(o);
        if (tmp) {
            countSub((MqMessageHolder) o);
        }
        return tmp;
    }

    /**
     * 计数加数
     */
    private void countAdd(MqMessageHolder mh) {
        int n = mh.getDistributeCount();

        if (n > 7) {
            _counters[8].increment();
        } else {
            _counters[n].increment();
        }
    }

    /**
     * 计数减数
     */
    private void countSub(MqMessageHolder mh) {
        int n = mh.getDistributeCount();

        if (n > 7) {
            _counters[8].decrement();
        } else {
            _counters[n].decrement();
        }
    }

    /**
     * 计数获取
     */
    public long countGet(int n) {
        if (n > 7) {
            return _counters[8].longValue();
        } else {
            return _counters[n].longValue();
        }
    }
}