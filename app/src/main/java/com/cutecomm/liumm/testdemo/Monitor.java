package com.cutecomm.liumm.testdemo;

import android.app.ActivityManager;
import android.content.Context;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 25817 on 2018/6/6.
 */

public class Monitor implements Runnable, CommandExecute.OnCommandListener {
    private static volatile Monitor instance = null;

    private ScheduledExecutorService executorService;
    /**
     * 频率:ms
     */
    private long freq = 1000;
    private RandomAccessFile procStatFile;
    private RandomAccessFile memInfoFile;

    private Double lastSystemCpuTotalTime;
    private Double lastSystemCpuIdleTime;

    private MonitorCallbacks callbacks;
    private Context context;
    private String cmd;
    private CommandExecute command;

    public interface MonitorCallbacks {

        /**
         * 系统CPU使用率回调
         * @param usage
         */
        void onSystemCpuUsage(String usage);

        /**
         * 系统内存使用率回调
         * @param usage
         */
        void onSystemMemoryUsage(String usage);

        /**
         * 系统执行命令返回数据
         * @param buffer
         * @param size
         */
        void onSystemCommandContent(byte[] buffer, int size);

        /**
         * 系统执行命令错误
         */
        void onSystemCommandError();

        /**
         * 系统执行命令完成
         */
        void onSystemCommandCompleted();
    }

    public static Monitor getInstance() {
        if (instance == null) {
            synchronized (Monitor.class) {
                if (instance == null) {
                    instance = new Monitor();
                }
            }
        }

        return instance;
    }

    protected Monitor() {

    }

    public void start(Context context, String cmd, MonitorCallbacks callbacks) {
        start(context, this.freq, cmd, callbacks);
    }

    public void start(Context context, long freq, String cmd, MonitorCallbacks callbacks) {
        this.freq = freq;
        this.cmd = cmd;
        startInternal(context, callbacks);
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }

        if (procStatFile != null) {
            try {
                procStatFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            procStatFile = null;
        }

        if (memInfoFile != null) {
            try {
                memInfoFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            memInfoFile = null;
        }

        if (command != null) {
            command.stop();
            command = null;
        }

        lastSystemCpuIdleTime = null;
        lastSystemCpuTotalTime = null;
        callbacks = null;
    }

    private void startInternal(Context context, MonitorCallbacks callbacks) {
        if (executorService == null) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        this.context = context;
        this.callbacks = callbacks;
        executorService.scheduleWithFixedDelay(this, 0, freq, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {

        if (callbacks != null) {
            callbacks.onSystemCpuUsage(getSystemCpuUsage());
            callbacks.onSystemMemoryUsage(getSystemMemoryUsage());
            command = new CommandExecute();
            command.start(this.cmd, this);
        }
    }

    ////////////////// OnCommandListener /////////////////////
    @Override
    public void onReceiveBuffer(byte[] buffer, int size) {
        if (callbacks != null) {
            callbacks.onSystemCommandContent(buffer, size);
        }
    }

    @Override
    public void onReadEnd() {
        if (callbacks != null) {
            callbacks.onSystemCommandCompleted();
        }
    }

    @Override
    public void onReadIOException() {
        if (callbacks != null) {
            callbacks.onSystemCommandError();
        }
    }

    @Override
    public void onRunning() {
        if (callbacks != null) {
            callbacks.onSystemCommandError();
        }
    }

    @Override
    public void onCommandInvalid() {
        if (callbacks != null) {
            callbacks.onSystemCommandError();
        }
    }

    /**
     * 获取系统CPU使用率
     * @return  返回百分比字符串
     */
    private String getSystemCpuUsage() {
        Double cpuTotalTime;
        Double cpuIdleTime;
        double usage = 0.0D;
        try {
            if (procStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
            } else {
                procStatFile.seek(0L);
            }
            String procStatString = procStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            cpuTotalTime = Double.parseDouble(procStats[2]) + Double.parseDouble(procStats[3])
                    + Double.parseDouble(procStats[4]) + Double.parseDouble(procStats[5])
                    + Double.parseDouble(procStats[6]) + Double.parseDouble(procStats[7])
                    + Double.parseDouble(procStats[8]) + Double.parseDouble(procStats[9]);
            cpuIdleTime = Double.parseDouble(procStats[5]);
            if (lastSystemCpuTotalTime == null) {
                lastSystemCpuTotalTime = cpuTotalTime;
                lastSystemCpuIdleTime = cpuIdleTime;
                return String.valueOf(usage) + "%";
            }
            if (0 != (cpuTotalTime - lastSystemCpuTotalTime)) {
                usage = DoubleUtil.div(100.00 * ((cpuTotalTime - cpuIdleTime) - (lastSystemCpuTotalTime - lastSystemCpuIdleTime)),
                        cpuTotalTime - lastSystemCpuTotalTime, 2);
            }
            lastSystemCpuTotalTime = cpuTotalTime;
            lastSystemCpuIdleTime = cpuIdleTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(usage) + "%";
    }

    private String getSystemMemoryUsage() {
        double usage = 0.0;

        long availableMemory = getAvailableMemory();
        long totalMemory = getTotalMemory();
        if (0 != totalMemory) {
            usage = DoubleUtil.div(100 * (totalMemory - availableMemory), totalMemory, 2);
        }
        return String.valueOf(usage) + "%";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位
     * @return
     */
    private long getAvailableMemory() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取系统总内存,返回以字节为单位
     * @return 系统总内存
     */
    private long getTotalMemory() {
        long totalMemorySize = 0;
        String dir = "/proc/meminfo";

        try {
            if (memInfoFile == null) {
                memInfoFile = new RandomAccessFile(dir, "r");
            } else {
                memInfoFile.seek(0L);
            }

            String memoryLine = memInfoFile.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            //将非数字的字符替换为空
            totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalMemorySize * 1024;
    }
}
