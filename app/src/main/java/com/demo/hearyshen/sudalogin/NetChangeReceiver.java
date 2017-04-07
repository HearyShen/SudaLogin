package com.demo.hearyshen.sudalogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NetChangeReceiver extends BroadcastReceiver {

    private static final String TAG="NetChangeReceiver";
    SharedPreferences preferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
        preferences = context.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        try {
            if( preferences.getBoolean("enableNetChangeLogin", false))
            {
//                Toast.makeText(context, "Connectivity change received!", Toast.LENGTH_SHORT).show();
                smartLogin(context);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "ERROR failed start Activity");
            e.printStackTrace();
        }


    }

    private void smartLogin(final Context context)
    {
        /*
        * SmartLogin 实现部分
        * 根据enableSmartLogin判断是否开启
        * 算法：根据账户连续登陆频数决定是否自动登陆
        * */

        preferences = context.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        if(preferences.getBoolean("enableSmartLogin",true)) {

            /* 获取连接信息（是否有活跃连接+是否通过WIFI连接+是否连接网络） */
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if(networkInfo!=null && networkInfo.getType()==ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {    // 当且仅当WiFi开启，才继续检查  wifiMgr.getWifiState()==WifiManager.WIFI_STATE_ENABLED

                /* 获取SSID */
                WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                String wifiSSID = wifiInfo.getSSID();
                //Toast.makeText(MainActivity.this, "wifi SSID=" + wifiSSID, Toast.LENGTH_SHORT).show();

                if(wifiSSID.toLowerCase().contains("suda")) {   // 当且仅当连接苏大网时，才允许自动登录

                    final Boolean enableNetChangeLogin = preferences.getBoolean("enableNetChangeLogin", false);
                    if (enableNetChangeLogin) {
                        // 开启子线程
                        new Thread() {
                            public void run() {
                                logoutByPost();
                                String result_str = loginByPost(preferences.getString("username",""), preferences.getString("password",""), preferences.getString("enableMacAuth","0"));
                                Toast.makeText(context, result_str, Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("netChangeLoginFrequency",preferences.getInt("netChangeLoginFrequency",0)+1);
                                editor.apply();
                            }

                        }.start();
                    }
                }
//                else{
//                    Toast.makeText(context, R.string.notSudaWifi_caption, Toast.LENGTH_SHORT).show();
//                }
//            }
//            else{
//                Toast.makeText(context, R.string.noWifi_caption, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * POST请求操作
     * （一键登陆快捷方式专用）
     */
    public void logoutByPost() {

        try {
            // 请求的地址
            String spec = "http://a.suda.edu.cn/index.php/index/logout";
            // 根据地址创建URL对象
            URL url = new URL(spec);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");

            urlConnection.setDoOutput(true); // 发送POST请求必须设置允许输出
            urlConnection.setDoInput(true); // 发送POST请求必须设置允许输入
            //setDoInput的默认值就是true

            // 获取响应的输入流对象
            InputStream is = urlConnection.getInputStream();
            // 创建字节输出流对象
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 定义读取的长度
            int len = 0;
            // 定义缓冲区
            byte buffer[] = new byte[1024];
            // 按照缓冲区的大小，循环读取
            while ((len = is.read(buffer)) != -1) {
                // 根据读取的长度写入到os对象中
                baos.write(buffer, 0, len);
            }
            // 返回字符串
            final String result = baos.toString();

            // 释放资源
            is.close();
            baos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * POST请求操作，
     * loginByPost通过POST请求，实现登陆功能
     * （一键登录快捷方式专用）
     *
     * @param userName
     * @param userPass
     * @param enableMacAuth "1"表示漫游免认证
     */
    public String loginByPost(String userName, String userPass, String enableMacAuth) {
        String result_print;
        try {
            // 请求的地址
            String spec = "http://a.suda.edu.cn/index.php/index/login";
            // 根据地址创建URL对象
            URL url = new URL(spec);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");

            String b64pass = Base64.encodeToString(userPass.getBytes(),Base64.DEFAULT);

            // 传递的数据
            String data = "username=" + URLEncoder.encode(userName, "UTF-8")
                    + "&password=" + URLEncoder.encode(b64pass, "UTF-8")
                    + "&enablemacauth="+enableMacAuth;

            urlConnection.setDoOutput(true); // 发送POST请求必须设置允许输出
            urlConnection.setDoInput(true); // 发送POST请求必须设置允许输入
            //setDoInput的默认值就是true
            //获取POST输出流
            OutputStream os = urlConnection.getOutputStream();
            os.write(data.getBytes());  // 字节流
            os.flush();     // 完整发送POST报文数据

            // 获取响应的输入流对象
            InputStream is = urlConnection.getInputStream();
            // 创建字节输出流对象
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 定义读取的长度
            int len = 0;
            // 定义缓冲区
            byte buffer[] = new byte[1024];
            // 按照缓冲区的大小，循环读取
            while ((len = is.read(buffer)) != -1) { // InputStream --read-> buffer(byte[])
                // 根据读取的长度写入到os对象中
                baos.write(buffer, 0, len);         // buffer(byte[]) --write-> ByteArrayOutputStream
            }
            // 返回字符串
            final String result = baos.toString();  // ByteArrayOutputStream -> String
//                final String result = new String(baos.toByteArray());

            // 释放资源
            is.close();
            baos.close();

            JSONObject jsobj = new JSONObject(result);
            int status = jsobj.getInt("status");
            if(status == 1){
                result_print = "网络感知 | "
                        +jsobj.getString("info")+"\n"
                        +"账号："+jsobj.getString("logout_username")+"\n"
                        +"认证域："+jsobj.getString("logout_domain")+"\n"
                        +"网关余额："+jsobj.getDouble("logout_account")+"\n"
                        +"本月时长："+jsobj.getInt("logout_timeamount")/3600+"小时"+jsobj.getInt("logout_timeamount")%3600/60+"分钟";
            }
            else
            {
                result_print = jsobj.getString("info");
            }

        } catch (Exception e) {
            result_print = "异常！";
            e.printStackTrace();
        }
        return result_print;
    }
}
