package org.noear.folkmq.server;

/**
 * @author noear
 * @since 1.0
 */
public class MqNextTime {
    /**
     * 是否允许派发
     * */
    public static boolean allowDistribute(MqMessageHolder messageHolder) {
        if (messageHolder.getNextTime() <= System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取下次派发时间
     * */
    public static long getNextTime(MqMessageHolder messageHolder) {
        switch (messageHolder.getTimes()) {
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
