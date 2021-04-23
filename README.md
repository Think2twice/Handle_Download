# Handle_Download
应用软件都有一个必需的功能：软件升级。当出现新版本时应用会提示升级并下载最新版本替换当前版本。在Android中这个过程可以通过HttpClient、AsyncTask和Handler结合实现。设计思路（实现原理）

1）	通过在AsyncTask访问服务器，得到最新版本号； 

2）	通过HttpClient连接服务器从服务器下载最新版本的apk； 

3）	通过Handler将下载的进度显示在界面上；
