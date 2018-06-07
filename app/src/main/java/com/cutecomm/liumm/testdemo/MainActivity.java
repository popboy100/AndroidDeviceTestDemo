package com.cutecomm.liumm.testdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Monitor.MonitorCallbacks {

    private static final String TAG = "monitor";

    private final String logFileName = "monitor.log";

    private FileOutputStream fileOutputStream;

    private TextView cpuTextView;
    private TextView memTextView;
    private Button startBtn;
    private Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cpuTextView = findViewById(R.id.cpu_tv);
        memTextView = findViewById(R.id.mem_tv);
        startBtn = findViewById(R.id.button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMonitor();
            }
        });
        stopBtn = findViewById(R.id.button2);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMonitor();
            }
        });
    }

    /**
     * 启动监控
     */
    private void startMonitor() {
        createMonitorLogFile();
        Monitor.getInstance().start(getApplicationContext(), 1000, "ps", this);
    }

    /**
     * 停止监控
     */
    private void stopMonitor() {
        Monitor.getInstance().stop();
        closeMonitorLogFile();
    }

    private void createMonitorLogFile() {
        if (!isExternalStorageMounted()) {
            Log.w(TAG, "External storage is not mounted.");
            return;
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory(), logFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                fileOutputStream = null;
            }
        }
    }

    private void closeMonitorLogFile() {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileOutputStream = null;
        }
    }

    private void saveMessageToMonitorLogFile(byte[] msg) {
        if (fileOutputStream != null && msg != null) {
            try {
                fileOutputStream.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * 判断外部存储是否可用
     * @return
     */
    private boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onSystemCpuUsage(final String usage) {
        //保存当前时间
        saveMessageToMonitorLogFile(getCurrentTime().getBytes());
        //写入换行符
        saveMessageToMonitorLogFile("\n".getBytes());
        String cpumsg = "系统CPU使用率：" + usage + "\n";
        saveMessageToMonitorLogFile(cpumsg.getBytes());
        Log.d(TAG, cpumsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cpuTextView.setText(usage);
            }
        });
    }

    @Override
    public void onSystemMemoryUsage(final String usage) {
        String memmsg = "系统内存使用率：" + usage + "\n";
        saveMessageToMonitorLogFile(memmsg.getBytes());
        Log.d(TAG, memmsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                memTextView.setText(usage);
            }
        });
    }

    @Override
    public void onSystemCommandContent(byte[] buffer, int size) {
        saveMessageToMonitorLogFile(buffer);
    }

    @Override
    public void onSystemCommandError() {
        Log.d(TAG, "Command error");
    }

    @Override
    public void onSystemCommandCompleted() {
        Log.d(TAG, "Command completed");
    }
}
