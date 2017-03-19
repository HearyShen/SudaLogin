package com.demo.hearyshen.sudalogin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    // 声明控件对象
    private EditText et_username, et_password;
    // 声明显示返回数据库的控件对象
    private TextView tv_result;
    private Button btn_login, btn_logout, btn_help;
    private Switch sw_macauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置显示的视图
        setContentView(R.layout.activity_main);
        // 通过 findViewById(id)方法获取用户名的控件对象
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_result = (TextView) findViewById(R.id.tv_result);
        sw_macauth = (Switch) findViewById(R.id.sw_macauth);

        //获得SharedPreferences对象，初始化显示信息
        final SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
        et_username.setText( preferences.getString("username", "") );
        et_password.setText( preferences.getString("password", "") );
        if( preferences.getString("enableMacAuth","").compareTo("1") == 0) {
            sw_macauth.setChecked(true);
        }
        else {
            sw_macauth.setChecked(false);
        }

        /*
        * SmartLogin 实现部分
        * 根据enableSmartLogin判断是否开启
        * 算法：根据账户连续登陆频数决定是否自动登陆
        * */
        if(preferences.getBoolean("enableSmartLogin",true)) {

            /* 获取连接信息（是否有活跃连接+是否通过WIFI连接+是否连接网络） */
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if(networkInfo!=null && networkInfo.getType()==ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {    // 当且仅当WiFi开启，才继续检查  wifiMgr.getWifiState()==WifiManager.WIFI_STATE_ENABLED

                /* 获取SSID */
                WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                String wifiSSID = wifiInfo.getSSID();
                //Toast.makeText(MainActivity.this, "wifi SSID=" + wifiSSID, Toast.LENGTH_SHORT).show();

                if(wifiSSID.toLowerCase().contains("suda")) {   // 当且仅当连接苏大网时，才允许自动登录

                    final int frequency = preferences.getInt("frequency", 0);
                    if (frequency >= 2) {
                        Toast.makeText(MainActivity.this, "智能登录", Toast.LENGTH_SHORT).show();
                        String waiting_title = "正在连接"+wifiSSID+"\n";
                        waiting_title = waiting_title.concat(getResources().getString(R.string.waiting_caption));
                        tv_result.setText(waiting_title);
                        // 开启子线程
                        new Thread() {
                            public void run() {
                                loginByPost(preferences.getString("username", ""), preferences.getString("password", "")); // 调用loginByPost方法
                            }

                            ;
                        }.start();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("frequency", frequency + 1);
                        editor.putInt("autoFrequency", preferences.getInt("autoFrequency",0) + 1);
                        editor.apply();     // 优化：apply自动优化写入时机，性能比占用同步资源的commit更好
                    }
                }
                else{
                    tv_result.setText(R.string.notSudaWifi_caption);
                }

            }
            else{
                tv_result.setText(R.string.noWifi_caption);
            }
        }

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户名
                final String userName = et_username.getText().toString();
                // 获取用户密码
                final String userPass = et_password.getText().toString();

                //获得SharedPreferences对象
                SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                /*
                * 分支逻辑：
                * 1. 账号、密码齐全，则登陆并记忆；
                * 2. 仅账号，则提示密码，并记忆账号；
                * 3. 无账号，则清除记忆，并更新到UI。
                * */
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(MainActivity.this, "记忆已清除！", Toast.LENGTH_LONG).show();
                    editor.clear();

                    // 通过runOnUiThread方法进行修改主线程的控件内容
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_result.setText("记忆已清除");
                            et_password.setText(null);
                            sw_macauth.setChecked(false);
                        }
                    });
                }
                else if(TextUtils.isEmpty(userPass)) {
                    et_password.setError("密码不能为空！");
                    editor.putString("username", userName);
                    editor.putString("password", null);
                    editor.putString("enableMacAuth",sw_macauth.isChecked()?"1":"0");
                    editor.putInt("frequency", 0);
                }
                else {
                    if(userName.compareTo(preferences.getString("username", "")) == 0) {    // 持续登陆(当前输入账号，和记忆的上一次登陆账号相同)，则记录登陆频数
                        editor.putInt("frequency", preferences.getInt("frequency",0) + 1);
                    }
                    else
                    {
                        editor.putString("username", userName);
                        editor.putInt("frequency", 0);
                    }
                    editor.putString("password", userPass);
                    editor.putString("enableMacAuth",sw_macauth.isChecked()?"1":"0");

                    Toast.makeText(MainActivity.this, "登陆请求已发送", Toast.LENGTH_SHORT).show();
                    tv_result.setText(R.string.waiting_caption);
                    // 开启子线程，处理网络IO
                        new Thread() {
                        public void run() {
                            loginByPost(userName, userPass); // 调用loginByPost方法
                        };
                    }.start();

                }
                if(!preferences.getBoolean("enableAutoSave",true)){   //若未开启自动记忆，则不记录
                    editor.putString("username", null);
                    editor.putString("password", null);
                    editor.putString("enableMacAuth","0");
                    editor.putInt("frequency", 0);
                }
                editor.apply();     // 优化：apply自动优化写入时机，性能比占用同步资源的commit更好
            }
        });

        btn_logout = (Button) findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "退出请求已发送", Toast.LENGTH_SHORT).show();
                // 开启子线程
                new Thread() {
                    public void run() {
                        logoutByPost(); // 调用loginByPost方法
                    };
                }.start();
            }
        });

        btn_help = (Button) findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpdialog();
            }
        });

    }

    /*
    * 重载onPause()
    * 实现自动保存
    * */
    @Override
    protected void onPause() {
        super.onPause();
        //获得SharedPreferences对象
        final SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
        if(preferences.getBoolean("enableAutoSave",true)){
            // 通过 findViewById(id)方法获取用户名的控件对象
            et_username = (EditText) findViewById(R.id.et_username);
            et_password = (EditText) findViewById(R.id.et_password);
            sw_macauth = (Switch) findViewById(R.id.sw_macauth);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", et_username.getText().toString());
            editor.putString("password", et_password.getText().toString());
            editor.putString("enableMacAuth",sw_macauth.isChecked()?"1":"0");

            editor.apply();
        }
    }

    /*
    * 重载onRestart
    * 实现设置更改后的页面更新
    * */
    @Override
    protected void onRestart() {
        super.onRestart();

        //获得SharedPreferences对象
        final SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        // 通过 findViewById(id)方法获取用户名的控件对象
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        sw_macauth = (Switch) findViewById(R.id.sw_macauth);

        et_username.setText(preferences.getString("username",""));
        et_password.setText(preferences.getString("password",""));
        sw_macauth.setChecked( preferences.getString("enableMacAuth","0").compareTo("1")==0 );
    }

    /**
     * POST请求操作，loginByPost通过POST请求，实现登陆功能
     *
     * @param userName
     * @param userPass
     */
    public void loginByPost(String userName, String userPass) {

        try {
            // 请求的地址
            String spec = "http://a.suda.edu.cn/index.php/index/login";
            // 根据地址创建URL对象
            URL url = new URL(spec);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");
//            // 设置请求的超时时间
//            urlConnection.setReadTimeout(5000);
//            urlConnection.setConnectTimeout(5000);

            String b64pass = Base64.encodeToString(userPass.getBytes(),Base64.DEFAULT);

            String enable_mac_auth = "0";
            if( sw_macauth.isChecked() )
                enable_mac_auth = "1";

            // 传递的数据
            String data = "username=" + URLEncoder.encode(userName, "UTF-8")
                    + "&password=" + URLEncoder.encode(b64pass, "UTF-8")
                    + "&enablemacauth="+enable_mac_auth;
            System.out.println(data);

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
            final String result_print;
            if(status == 1){
                 result_print = jsobj.getString("info")+"\n"
                        +"账号："+jsobj.getString("logout_username")+"\n"
                        +"IP："+jsobj.getString("logout_ip")+"\n"
                        +"位置："+jsobj.getString("logout_location")+"\n"
                        +"认证域："+jsobj.getString("logout_domain")+"\n"
                         +"网关余额："+jsobj.getDouble("logout_account")+"\n"
                        +"本月时长："+jsobj.getInt("logout_timeamount")/3600+"小时"+jsobj.getInt("logout_timeamount")%3600/60+"分钟";
            }
            else
            {
                result_print = jsobj.getString("info");
            }

            // 通过runOnUiThread方法进行修改主线程的控件内容
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_result.setText(result_print);
                    System.out.println(result);
                    System.out.println(result_print);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * POST请求操作
     *
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
//
//            if (urlConnection.getResponseCode() == 200) {
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

                JSONObject jsobj = new JSONObject(result);
                int status = jsobj.getInt("status");
                final String result_print;
                if(status == 1){
                    result_print = jsobj.getString("info");
                }
                else
                {
                    result_print = jsobj.getString("info");
                }

                // 通过runOnUiThread方法进行修改主线程的控件内容
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_result.setText(result_print);
                        System.out.println(result);
                        System.out.println(result_print);
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void helpdialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setMessage(R.string.help_info);
//        builder.setTitle("帮助");
//        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        startActivity(intent);
    }
}
