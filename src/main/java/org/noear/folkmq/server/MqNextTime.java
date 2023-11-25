package org.noear.folkmq.server;

/**
 * 派发时间生成器
 *
 * @author noear
 * @since 1.0
 */
public class MqNextTime {
    private static long maxDelayMillis = 1000 * 60 * 60 * 2;

    /**
     * 最大延时毫秒数（2小时）
     * */
    public static long getMaxDelayMillis() {
        return maxDelayMillis;
    }

    /**
     * 获取下次派发时间
     *
     * @param messageHolder 消息
     * */
    public static long getNextTime(MqMessageHolder messageHolder) {
        switch (messageHolder.getDistributeCount()) {
            case 0:
                return 0;
            case 1:
                return System.currentTimeMillis() + 1000 * 5; //5s
            case 2:
                return System.currentTimeMillis() + 1000 * 30; //30s
            case 3:
                return System.currentTimeMillis() + 1000 * 60 * 3; //3m
            case 4:
                return System.currentTimeMillis() + 1000 * 60 * 9; //9m
            case 5:
                return System.currentTimeMillis() + 1000 * 60 * 15; //15m
            case 6:
                return System.currentTimeMillis() + 1000 * 60 * 30; //30m
            case 7:
                return System.currentTimeMillis() + 1000 * 60 * 60; //60m
            default:
                return System.currentTimeMillis() + 1000 * 60 * 60 * 2; //120m
        }
    }
}
