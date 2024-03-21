package org.noear.folkmq.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 打包循环器（一个个添加；打包后批量处理）
 * 提交时，每次提交100条；消费完后暂停0.1秒
 *
 * @author noear
 * @since 1.3
 * */
public class PackagingLoopImpl<Event> implements PackagingLoop<Event>, Closeable {
    /**
     * 休息时间
     */
    private long idleInterval = 500; //必须大于等于min
    private final long idleInterval_min = 10;

    /**
     * 包装合大小
     */
    private int packetSize = 150; //必须大于等于150
    private final int packetSize_min = 1;

    private Thread workThread;

    private PackagingWorkHandler<Event> workHandler;

    public PackagingLoopImpl() {
        workThread = new Thread(() -> {
            workStartDo();
        }, "Simple task");

        workThread.start();
    }

    public PackagingLoopImpl(long idleInterval, int packetSize, PackagingWorkHandler<Event> workHandler) {
        this();

        setIdleInterval(idleInterval);
        setPacketSize(packetSize);
        setWorkHandler(workHandler);
    }


    public void setWorkHandler(PackagingWorkHandler<Event> workHandler) {
        this.workHandler = workHandler;
    }


    //
    //
    //

    private Queue<Event> queueLocal = new LinkedBlockingQueue<>();

    public void add(Event event) {
        try {
            queueLocal.add(event);
        } catch (Exception ex) {
            //不打印，不推出
            ex.printStackTrace();
        }
    }

    public void addAll(Collection<Event> events) {
        try {
            queueLocal.addAll(events);
        } catch (Exception ex) {
            //不打印，不推出
            ex.printStackTrace();
        }
    }

    /**
     * 空闲休息时间
     */
    public long getIdleInterval() {
        return idleInterval;
    }

    /**
     * 设置空闲休息时间
     */
    public void setIdleInterval(long idleInterval) {
        if (idleInterval >= idleInterval_min) {
            this.idleInterval = idleInterval;
        }
    }

    /**
     * 设置包装合大小
     */
    public void setPacketSize(int packetSize) {
        if (packetSize >= packetSize_min) {
            this.packetSize = packetSize;
        }
    }

    //
    // 打包处理控制
    //

    private void workStartDo() {
        while (true) {
            if (isStopped) {
                return;
            }

            try {
                long time_start = System.currentTimeMillis();
                this.workExecDo();
                long time_end = System.currentTimeMillis();

                if (this.getIdleInterval() == 0) {
                    return;
                }

                if ((time_end - time_start) < this.getIdleInterval()) {
                    Thread.sleep(this.getIdleInterval());
                }

            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private void workExecDo() throws Exception {
        if (workHandler == null) {
            return;
        }

        while (true) {
            if (isStopped) {
                return;
            }

            List<Event> list = new ArrayList<>(packetSize);

            collectDo(list);

            if (list.size() > 0) {
                workHandler.doWork(list);
            } else {
                break;
            }
        }
    }

    private void collectDo(List<Event> list) {
        //收集最多100条日志
        //
        int count = 0;
        while (true) {
            if (isStopped) {
                return;
            }

            Event event = queueLocal.poll();

            if (event == null) {
                break;
            } else {
                list.add(event);
                count++;

                if (count == packetSize) {
                    break;
                }
            }
        }
    }

    private boolean isStopped = false;

    @Override
    public void close() throws IOException {
        isStopped = true;
    }
}
