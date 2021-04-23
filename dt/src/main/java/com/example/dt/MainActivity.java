package com.example.dt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private Context context;

    public void setContext(Context contextf) {
        context = contextf;
    }

    //读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    private ProgressDialog pd;
    private static final String TAG = "DownloadTest";
    private String getUrl = "http://60.166.83.74:8080/apk/Surfboard.apk";

    private static final int DOWNLOAD_FILE_CODE = 100001;
    private static final String DOWNLOAD_URL = "http://192.168.43.27:8080/apk/Surfboard.apk";
    private static final int DOWNLOAD_FILE_FAILE_CODE = 100002;
    private Button StartDownload;
    private TextView percent;
    private ProgressBar progressBar;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        StartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //执行下载程序
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        download(DOWNLOAD_URL);
                        //download();
                    }
                }).start();

            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 100001:
                        //更新进度条
                        progressBar.setProgress((Integer) msg.obj);
                        percent.setText(String.valueOf(msg.arg1) + "%");
                        if (progressBar.getProgress() == 100) {
                            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 100002:
                        Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };
    }


    private void download(String AppUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //实例化URL对象
                try {

                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(AppUrl);
                    HttpResponse response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        //获取下载路径
                        String path = getApplicationContext().getFilesDir().getAbsolutePath()
                                + File.separator + "apk" + File.separator;

                        /*String path = "C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\ROOT\\apk";*/
                        //File file = new File(Environment.getExternalStorageDirectory(),"Surfboard.apk");
                        //创建目录
                        File PathName = new File(path);
                        //如果PathName不存在的话 就创建这个目录
                        if (!PathName.exists()) {
                            PathName.mkdirs();//mkdirs()用于创建多层目录
                        }
                        //有了目录之后，就需要一个文件名
                        String ApkName = path + "Surfboard.apk";
                        //判断一下这个文件是否已经存在，存在的话就删除它
                        File ApkFile = new File(ApkName);
                        if (ApkFile.exists()) {
                            ApkFile.delete();
                        }
                        //获取文件的总长度
                        int ContentLength = (int) entity.getContentLength();
                        //获取输入流
                        InputStream in = entity.getContent();
                        byte[] b = new byte[1024];
                        int DownloadLength = 0; //用于保存实时下载长度
                        int len = 0;
                        OutputStream out = new FileOutputStream(ApkName);
                        while ((len = in.read(b)) > -1) {
                            out.write(b, 0, len);
                            DownloadLength += len;
                            //将实时的下载长度传给UI线程
                            Message message = handler.obtainMessage();
                            message.what = DOWNLOAD_FILE_CODE;
                            message.obj = DownloadLength * 100 / ContentLength;
                            message.arg1 = (int) (DownloadLength * 100 / ContentLength);
                            handler.sendMessage(message);
                            //installApk(ApkFile);
                            //openFile(ApkFile);
                        }
                    }

                } catch (java.io.IOException e) {
                    downloadfail();
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void downloadfail() {
        Message message = handler.obtainMessage();
        message.what = DOWNLOAD_FILE_FAILE_CODE;
        handler.sendMessage(message);
    }

    private void initView() {
        StartDownload = (Button) findViewById(R.id.btn_start);
        percent = (TextView) findViewById(R.id.percent);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }


    //检查权限
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}