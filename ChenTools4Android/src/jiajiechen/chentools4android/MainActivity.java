package jiajiechen.chentools4android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import jiajiechen.chentools.ChenTools;
import jiajiechen.chentools.ChenTools.InputOutput;

public class MainActivity extends Activity implements InputOutput {

    private Spinner spinner;
    private static TextView console;
    private ArrayAdapter<String> adapter;
    private static EditText entertext;
    private static Button enter;
    private boolean syncLock;
    private int currentSelect;
    private TextView versiontext;

    private static MainActivity INSTANCE;

    private static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    enter.setEnabled(false);
                    break;
                case 1:
                    enter.setEnabled(true);
                    break;
                case 2:
                    String appendString = (String) msg.obj;
                    CharSequence orig = console.getText();
                    console.setText(orig + appendString);
                    break;
                case 3:
                    entertext.setText("");
                    break;
                case 4:
                    entertext.setInputType((Integer) msg.obj);
                    break;
                case 5:
                    String version = (String) msg.obj;
                    if (version.equals("")) {
                        new AlertDialog.Builder(MainActivity.INSTANCE).setTitle("更新")
                                .setMessage("无法检测更新！").show();
                    }
                    if (version.compareTo(ChenTools.version) > 0) {
                        new AlertDialog.Builder(MainActivity.INSTANCE)
                                .setTitle("更新")
                                .setMessage("检测到新版本！要打开下载链接吗？")
                                .setPositiveButton("跳转",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Intent viewIntent = new

                                                        Intent(
                                                        "android.intent.action.VIEW",
                                                        Uri.parse("https://github.com/wiadufachen/ChenTools/blob/master/ChenTools4Android-release.apk?raw=true"));
                                                MainActivity.INSTANCE.startActivity(viewIntent);
                                            }
                                        }).setNegativeButton("取消", null).show();
                    }
                    break;
                case 6:
                    entertext.requestFocus();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        INSTANCE = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner1);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ChenTools.m);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner.setVisibility(View.VISIBLE);
        console = (TextView) findViewById(R.id.consoleview);
        entertext = (EditText) findViewById(R.id.entertext);
        enter = (Button) findViewById(R.id.enter);
        enter.setEnabled(false);
        entertext.setText("");
        console.setText("");
        syncLock = true;
        enter.setOnClickListener(new OnClickListener() {
            public void onClick(View w) {
                MainActivity.INSTANCE.unlock();
            }
        });
        ChenTools.io = this;
        versiontext = (TextView) findViewById(R.id.versiontext);
        versiontext.setText("ChenTools v" + ChenTools.version);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String version = ChenTools
                        .httpDownload("https://github.com/wiadufachen/ChenTools/blob/master/LATEST?raw=true");
                mHandler.obtainMessage(5, version);
            }
        });
        thread.start();
    }

    public void wantNumber() {
        mHandler.obtainMessage(4, InputType.TYPE_CLASS_NUMBER).sendToTarget();
    }

    public void wantText() {
        mHandler.obtainMessage(4, InputType.TYPE_CLASS_TEXT).sendToTarget();
    }

    public void emptyConsole() {
        console.setText("");
    }

    public synchronized boolean lock() throws InterruptedException {
        while (syncLock == true) {
            MainActivity.INSTANCE.wait();
        }
        syncLock = true;
        return true;
    }

    public synchronized void unlock() {
        syncLock = false;
        MainActivity.INSTANCE.notifyAll();
    }

    public void writeToConsole(String str) {
        mHandler.obtainMessage(2, str).sendToTarget();
    }

    public String getInput() throws InterruptedException {
        mHandler.obtainMessage(1, "").sendToTarget();
        mHandler.obtainMessage(6, "").sendToTarget();
        lock();
        String ret = entertext.getText().toString();
        mHandler.obtainMessage(0, "").sendToTarget();
        mHandler.obtainMessage(3, "").sendToTarget();
        return ret;
    }

    public void inputError() {
        new AlertDialog.Builder(this).setTitle("错误").setMessage("请重新输入！")
                .show();
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ChenTools.work(currentSelect);
        }
    };

    class SpinnerSelectedListener implements OnItemSelectedListener {
        public Thread thread = null;

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            currentSelect = arg2;
            MainActivity.INSTANCE.emptyConsole();
            if (thread != null) {
                thread.interrupt();
            }
            thread = new Thread(runnable);
            thread.start();
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

}
