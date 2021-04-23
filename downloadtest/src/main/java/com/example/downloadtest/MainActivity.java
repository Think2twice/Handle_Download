package com.example.downloadtest;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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

public class MainActivity extends AppCompatActivity {
    //读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private static final String TAG = "DownloadTest";
    private String getUrl = "http://192.168.43.27:8080/apk/Surfboard.apk";
    private ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * 绑定button的事件
     * @param view
     */
    public void down(View view){
        Dialog dialog =new AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage("当前版本：1.2\r\n最新版本：1.3")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        download();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
    }

    /**
     * 下载apk
     */
    public void download() {
        pd = new ProgressDialog(this);
        pd.setMessage("正在更新，请稍后。。。");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
        //连接网络下载，新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(getUrl);
                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        //判断sd卡是否安装
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            //下载apk存放位置
                           /* ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                            File file = new File(directory, "Surfboard" + ".apk");*/

                            File file = new File(Environment.getExternalStorageDirectory(), "Surfboard.apk");//文件路径
                            FileOutputStream fos = new FileOutputStream(file);//创建文件的输出流
                            InputStream is = response.getEntity().getContent();//服务器返回的流
                            BufferedInputStream bis = new BufferedInputStream(is);
                            //apk总大小
                            int total =(int)response.getEntity().getContentLength();
                            pd.setMax(total);
                            //写入文件
                            byte[] buffer = new byte[1024];//一次读取1024字节
                            int len;
                            int pro = 0;
                            while ((len = bis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                pro += len;
                                pd.setProgress(pro);
                            }
                            fos.close();
                            bis.close();
                            is.close();
                            pd.dismiss();
                            //安装apk
                            installApk(file);

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void installApk(File file) {
        Intent intent = new Intent();
        //执行显示的动作
        intent.setAction(Intent.ACTION_VIEW);
        //在新的任务栈中启动activity（添加这句话以后，会提示用户打开或者完成）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //执行的数据类型MIME类型
        //apk的MIME类型为：application/vnd.android.package-archive
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);


    }

    //检查权限
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
//安装apk
   /* private void installApk(File file) {
        File apkfile = new File("路径", "文件名");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }*/

/*private void clidown(String Appurl){
        HttpURLConnection conn;
        Bitmap bitmap;
        try {
            //创建URL对象
            URL url = new URL(Appurl);
            //根据url发送http的请求
            conn = (HttpURLConnection) url.openConnection();
            //设置请求的方式
            conn.setRequestMethod("GET");
            //设置超时时间
            conn.setConnectTimeout(5000);
            //得到服务器返回码是200
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

