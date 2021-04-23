package com.example.handle_download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
    //private static final int REQUEST_INSTALL_CODE = 1;
    private Context mContext;

    public void setContext(Context context) {
        this.mContext = context;
    }



    //读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


//    private ProgressDialog pd;
//    private static final String TAG = "DownloadTest";
//    private String getUrl = "http://60.166.83.74:8080/apk/Surfboard.apk";

    private static final int DOWNLOAD_FILE_CODE = 100001;
    private static final String DOWNLOAD_URL = "http://223.240.237.86:8080/apk/Handle_Download.apk";
    private static final int DOWNLOAD_FILE_FAILE_CODE = 100002;
    private Button StartDownload;
    private TextView percent;
    private ProgressBar progressBar;
    private Handler handler;
    private TextView textView;
    private TextView tv;


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
                        new MyTask().execute("http://223.240.237.86:8080/");
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
    /*public void download(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();

                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                        //判断sd卡是否安装
                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                            File file = new File(Environment.getExternalStorageDirectory(),"Surfboard.apk");
                            FileOutputStream fos = new FileOutputStream(file);//创建文件的输出流
                            InputStream is = response.getEntity().getContent();//服务器返回流
                            BufferedInputStream bis = new BufferedInputStream(is);
                            //apk总大小
                            int total = (int) response.getEntity().getContentLength();
                            pd.setMax(total);
                            //写入文件
                            byte[] buffer = new byte[1024];
                            int len;
                            int pro = 0;
                            while((len = bis.read(buffer)) != -1){
                                fos.write(buffer,0,len);
                                pro += len;
                                pd.setProgress(pro);
                            }
                            fos.close();
                            is.close();
                            bis.close();
                            pd.dismiss();
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


    }*/

    class MyTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {//单纯运行一遍
            super.onPreExecute();
            Log.i("测试开始", "测试开始");
            //tv.setText("当前版本2");
            //tv.setText("loading...");
        }

        @Override
        protected String doInBackground(String... strings) {//字符数组，传过来的三个数据012
            Log.i("连接之前", "连接之前。。。");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet jsonGet = new HttpGet(strings[0] + "NewsInfo.json");//读取string连接tomacat 的joson文件
                HttpResponse jsonRes = client.execute(jsonGet);
                if (jsonRes.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = jsonRes.getEntity();
                    String str = EntityUtils.toString(entity);
                    Log.i("连接之后", str);
                    entity.consumeContent();
                    return str;//返回json里面的数据，转换成字符串
                }else {
                    Log.i("连接失败", "连接失败");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {//返回到这里
            super.onPostExecute(s);
            if (s != null) {
                try {
                    //创建一个JSON对象
                    Log.i("S的数据", s.toString());
                    JSONObject jsonObject = new JSONObject(s.toString());//解析json
                    int newVersion = jsonObject.getInt("versionCode");
                    PackageManager manager = MainActivity.this.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                    int nowVersion = info.versionCode;
                    //tv.setText("当前版本" + String.valueOf(nowVersion));
//                    Log.i("name", jsonObject.getString("name"));
//                    Log.i("version", jsonObject.getString("version"));
//                    Log.i("versionName", jsonObject.getString("versionName"));
                    if(nowVersion < newVersion){
                        AlertDialog dialog;
                        dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("检测到新版本！")
                                .setIcon(R.mipmap.ic_launcher)
                                .setMessage("1.name = " + jsonObject.getString("name") +
                                        "\n2.version = " + jsonObject.getString("version") +
                                        "\n3.versionCode = " + newVersion)
                                .create();
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tv.setText("这是旧版本");
                                Toast.makeText(MainActivity.this, "您没有选择更新", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*if (Build.VERSION.SDK_INT >= 23) {
                                    int REQUEST_CODE_CONTACT = 101;
                                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                    //验证是否许可权限
                                    for (String str : permissions) {
                                        if (MainActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                            //申请权限
                                            MainActivity.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                                            return;
                                        } else {*/
                                progressBar.setVisibility(View.VISIBLE);
                                percent.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.VISIBLE);
                                download(DOWNLOAD_URL);
                                        /*}
                                    }
                                }*/
                            }
                        });
                        dialog.show();
                    }else{
                        tv.setText("当前已经是最新版本！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
                        //String path = Environment.getExternalStorageDirectory();
                        /*String path = "C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\ROOT\\apk";*/
                        //File file = new File(Environment.getExternalStorageDirectory(),"Surfboard.apk");
                        //创建目录
                        File PathName = new File(path);
                        //如果PathName不存在的话 就创建这个目录
                        if (!PathName.exists()) {
                            PathName.mkdirs();//mkdirs()用于创建多层目录
                        }
                        //有了目录之后，就需要一个文件名
                        String ApkName = path + "Handle_Download.apk";
                        //判断一下这个文件是否已经存在，存在的话就删除它
                        File ApkFile = new File(ApkName);
                        //File file = new File(Environment.getExternalStorageDirectory(), "Surfboard.apk");
                        if (ApkFile.exists()) {
                            installApk(ApkFile);
                            return;
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

                            //openFile(ApkFile);
                        }
                        in.close();
                        out.close();
                        installApk(ApkFile);

                    }

                } catch (java.io.IOException e) {
                    downloadfail();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /*private void installApk(File file) {
        //调用系统安装程序
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        //intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        //startActivityForResult(intent, REQUEST_INSTALL_CODE);
        //startActivityForResult(intent,REQUEST_EXTERNAL_STORAGE);
    }*/
    private void installApk(File file) {
        Intent intent = new Intent();
        //执行显示的动作
        intent.setAction(Intent.ACTION_VIEW);
        //在新的任务栈中启动activity（添加这句话以后，会提示用户打开或者完成）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName()+".fileprovider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        //执行的数据类型MIME类型
        //apk的MIME类型为：application/vnd.android.package-archive
        //intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);

    }
    /*private void installApk(File file) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Log.i("文件信息",file.toString());
        Log.i("安装位置",getApplicationContext().getPackageName()+".fileprovider");
        Uri apkUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName()+".fileprovider", file);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        MainActivity.this.startActivity(intent);
        finish();
    }*/


    private void downloadfail() {
        Message message = handler.obtainMessage();
        message.what = DOWNLOAD_FILE_FAILE_CODE;
        handler.sendMessage(message);
    }

    private void initView() {
        StartDownload = (Button) findViewById(R.id.btn_start);
        percent = (TextView) findViewById(R.id.percent);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        tv = (TextView) findViewById(R.id.tv_msg);
        progressBar.setVisibility(View.INVISIBLE);
        percent.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
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

    /*private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }*/
    /*private void installApk(File file) {
        Intent intent = new Intent();
        //执行显示的动作
        intent.setAction(Intent.ACTION_VIEW);
        //在新的任务栈中启动activity（添加这句话以后，会提示用户打开或者完成）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //执行的数据类型MIME类型
        //apk的MIME类型为：application/vnd.android.package-archive
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);

        *//*Intent install = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri;
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/Surfboard.apk");
        if (file != null) {
            String path = file.getAbsolutePath();
            downloadFileUri = Uri.parse("file://" + path);
            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        }*//*
    }*/


//        new File("File:///storage/emulated/0/Surfboard.apk"))
//new File("/mnt/sdcard/Download/update.apk")
/*private void installApk(File file) {
 *//*File apkfile = new File("/storage/emulated/0/Surfboard.apk", "Surfboard.apk");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        startActivity(intent);*//*
        File toInstall = new File("file:///storage/emulated/0/Surfboard.apk", "Surfboard.apk");
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", toInstall);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        }
        startActivity(intent);
    }*/