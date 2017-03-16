package com.demo.hearyshen.sudalogin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
        // 通过 findViewById(id)方法获取用户密码的控件对象
        et_password = (EditText) findViewById(R.id.et_password);

        // 通过 findViewById(id)方法获取显示返回数据的控件对象
        tv_result = (TextView) findViewById(R.id.tv_result);

        sw_macauth = (Switch) findViewById(R.id.sw_macauth);

        //获得SharedPreferences对象
        final SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
        et_username.setText( preferences.getString("username", "") );
        et_password.setText( preferences.getString("password", "") );
        if( preferences.getString("enableMacAuth","").compareTo("1") == 0) {
            sw_macauth.setChecked(true);
//            Toast.makeText(MainActivity.this, "默认勾选", Toast.LENGTH_SHORT).show();
        }
        else {
            sw_macauth.setChecked(false);
//            Toast.makeText(MainActivity.this, "默认未选", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(MainActivity.this, "账号记忆："+preferences.getString("username", "")+preferences.getString("enableMacAuth",""), Toast.LENGTH_SHORT).show();
        final int frequency = preferences.getInt("frequency",0);
        //Toast.makeText(MainActivity.this, "登录次数: "+frequency, Toast.LENGTH_SHORT).show();
        if(frequency >= 2){
            Toast.makeText(MainActivity.this, "自动登录中", Toast.LENGTH_SHORT).show();
            tv_result.setText(R.string.waiting_caption);
            // 开启子线程
            new Thread() {
                public void run() {
                    loginByPost(preferences.getString("username",""), preferences.getString("password","")); // 调用loginByPost方法
                };
            }.start();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("frequency", frequency+1);
            editor.commit();
        }

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户名
                final String userName = et_username.getText().toString();
                // 获取用户密码
                final String userPass = et_password.getText().toString();

                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPass)) {
                    Toast.makeText(MainActivity.this, "用户名或者密码不能为空", Toast.LENGTH_LONG).show();
                } else {
                    //获得SharedPreferences对象
                    SharedPreferences preferences = MainActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    if(userName.compareTo(preferences.getString("username", "")) == 0) {    // 持续登陆(当前输入账号，和记忆的上一次登陆账号相同)，则记录登陆频数
                        editor.putInt("frequency", preferences.getInt("frequency",0) + 1);
                    }
                    else
                    {
                        editor.putInt("frequency", 0);
                    }
                    editor.putString("username", userName);
                    editor.putString("password", userPass);
                    editor.putString("enableMacAuth",sw_macauth.isChecked()?"1":"0");
                    editor.commit();

                    Toast.makeText(MainActivity.this, "登陆请求已发送", Toast.LENGTH_SHORT).show();
                    tv_result.setText(R.string.waiting_caption);
                    // 开启子线程
                        new Thread() {
                        public void run() {
                            loginByPost(userName, userPass); // 调用loginByPost方法
                        };
                    }.start();

                }
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

    /**
     * POST请求操作
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
            //获取输出流
            OutputStream os = urlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();

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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.help_info);
        builder.setTitle("帮助");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
