package org.noear.folkmq.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author noear
 * @since 1.8
 */
public class SnowflakeId {
    public static final SnowflakeId DEFAULT = new SnowflakeId(1,1);

    //默认起始时间 2020-01-01 00:00:00（差不多可以用69年）
    private static final long START_TIME_DEF = 1577808000000L;

    public SnowflakeId(long dataId, long workId) {
        this(dataId, workId, START_TIME_DEF);
    }

    public SnowflakeId(long dataId, long workId, long startTime) {
        if (dataId > dataMaxNum || dataId < 0) {
            throw new IllegalArgumentException("dataId can't be greater than DATA_MAX_NUM or less than 0");
        }
        if (workId > workMaxNum || workId < 0) {
            throw new IllegalArgumentException("workId can't be greater than WORK_MAX_NUM or less than 0");
        }

        //默认起始时间 2020-01-01 00:00:00
        if (startTime > 0) {
            this.startTime = startTime;
        } else {
            this.startTime = START_TIME_DEF;
        }

        this.dataId = dataId;
        this.workId = workId;
    }

    /// /////////////////////////

    //时间部分所占长度(用69年)
    private final int timeLen = 41;
    //数据中心id所占长度
    private final int dataLen = 5;
    //机器id所占长度
    private final int workLen = 5;
    //毫秒内序列所占长度
    private final int seqLen = 12;
    //定义起始时间
    private final long startTime;
    //上次生成ID的时间截
    private long lastTimeStamp = -1L;
    //时间部分向左移动的位数 22
    private final int timeLeftBit = 64 - 1 - timeLen;

    //自动获取数据中心id（可以手动定义 0-31之间的数）
    private final long dataId;
    //自动机器id（可以手动定义 0-31之间的数）
    private final long workId;
    //数据中心id最大值 31
    private final int dataMaxNum = ~(-1 << dataLen);
    //机器id最大值 31
    private final int workMaxNum = ~(-1 << workLen);
    //随机获取数据中心id的参数 32
    private final int dataRandom = dataMaxNum + 1;
    //随机获取机器id的参数 32
    private final int workRandom = workMaxNum + 1;
    //数据中心id左移位数 17
    private final int dataLeftBit = timeLeftBit - dataLen;
    //机器id左移位数 12
    private final int workLeftBit = dataLeftBit - workLen;

    //上一次的毫秒内序列值
    private long seqLastVal = 0L;
    //毫秒内序列的最大值 4095
    private final long seqMaxNum = ~(-1 << seqLen);

    private final ReentrantLock ID_LOCK = new ReentrantLock();


    /**
     * 获取下一个Id
     */
    public long nextId() {
        ID_LOCK.lock();

        try {
            return nextId0();
        } finally {
            ID_LOCK.unlock();
        }
    }

    private long nextId0() {
        long now = System.currentTimeMillis();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (now < lastTimeStamp) {
            throw new IllegalStateException("System time error refused to generate snowflake ID!");
        }

        if (now == lastTimeStamp) {
            seqLastVal = (seqLastVal + 1) & seqMaxNum;
            if (seqLastVal == 0) {
                now = nextMillis(lastTimeStamp);
            }
        } else {
            seqLastVal = 0;
        }

        //上次生成ID的时间截
        lastTimeStamp = now;

        return ((now - startTime) << timeLeftBit) | (dataId << dataLeftBit) | (workId << workLeftBit) | seqLastVal;
    }


    /**
     * 获取下一不同毫秒的时间戳，不能与最后的时间戳一样
     */
    protected long nextMillis(long lastMillis) {
        long now = System.currentTimeMillis();
        while (now <= lastMillis) {
            now = System.currentTimeMillis();
        }
        return now;
    }
}
