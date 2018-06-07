package com.cutecomm.liumm.testdemo;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class CommandExecute {
    private static final String TAG = "monitor";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 10;
    private InputStream inputStream;
    private Process process;
    private volatile boolean stopped = false;
    private volatile boolean running = false;
    private OnCommandListener commandListener;
    private String cmd;

    public interface OnCommandListener {
        void onReceiveBuffer(byte[] buffer, int size);

        void onReadEnd();

        void onReadIOException();

        void onRunning();

        void onCommandInvalid();
    }

    public void start(String cmd, OnCommandListener listener) {
        this.cmd = cmd;
        this.commandListener = listener;
        runCommand();
    }


    public boolean isRunning() {
        return running;
    }

    public void stop() {
        if (!isRunning()) {
            return;
        }

        stopped = false;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = null;
        }

        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    synchronized public void runCommand() {
        if (isRunning()) {
            if (commandListener != null) {
                commandListener.onRunning();
            }
            return;
        }

        if (TextUtils.isEmpty(cmd)) {
            if (commandListener != null) {
                commandListener.onCommandInvalid();
            }
            return;
        }

        Log.d(TAG, "start run command:" + cmd);
        running = true;
        String[] command = cmd.split("\\s");
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {

            process = builder.start();
            inputStream = process.getInputStream();

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            while (!stopped) {
                int size = inputStream.read(buffer);

                if (size < 0) {
                    Log.d(TAG, "read command size < 0" + size);
                    if (commandListener != null) {
                        stop();
                        commandListener.onReadEnd();
                    }
                    break;
                }

                if (commandListener != null) {
                    commandListener.onReceiveBuffer(buffer, size);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            if (!stopped) {
                if (commandListener != null) {
                    byte[] buffer = e.getMessage().getBytes();
                    if (buffer != null) {
                        commandListener.onReceiveBuffer(buffer, buffer.length);
                    }
                    commandListener.onReadIOException();
                }
            }
        } finally {
            running = false;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }

            if (process != null) {
                process.destroy();
                process = null;
            }
        }
    }
}

