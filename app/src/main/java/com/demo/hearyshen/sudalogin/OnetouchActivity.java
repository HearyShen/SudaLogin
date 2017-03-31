package com.demo.hearyshen.sudalogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OnetouchActivity extends AppCompatActivity {

    private SharedPreferences prefers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 初始化快捷方式图标 */
        if(createShortCut()) {
//            Toast.makeText(OnetouchActivity.this, "TestLog: Creating SHORTCUT", Toast.LENGTH_SHORT).show();
            return;
        }
        moveTaskToBack(true);   //防止调到后台MainActivity

//        Toast.makeText(OnetouchActivity.this, "TestLog: sending POST", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                logoutByPost();
                prefers = OnetouchActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);
                loginByPost(prefers.getString("username",""), prefers.getString("password",""), prefers.getString("enableMacAuth","0"));
                SharedPreferences.Editor editor = prefers.edit();
                editor.putInt("oneTouchFrequency", prefers.getInt("oneTouchFrequency",0)+1);
                editor.apply();
            }
        }).start();
        finish();

    }

//    @Override
//    protected void onResume() {
//        finish();
//        super.onResume();
//    }

    /*
    * createShortCut
    * 初次建立图标，则返回true
    * 否则返回false
    * */
    public boolean createShortCut(){
        Intent addShortCut;
        //判断是否需要添加快捷方式
        try {
            if (getIntent().getAction().compareTo(Intent.ACTION_CREATE_SHORTCUT) == 0) {
                addShortCut = new Intent();
                addShortCut.putExtra("duplicate", false);
                //快捷方式的名称
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.oneTouchLogin_title));
                //显示的图片
                Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.onetouch_icon);
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                //快捷方式激活的activity，需要执行的intent，自己定义
                addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(OnetouchActivity.this, OnetouchActivity.class));
                //OK，生成
                setResult(RESULT_OK, addShortCut);
                finish();
                return true;

            } else {
                //取消
                setResult(RESULT_CANCELED);
                return false;
            }
        }
        catch (Exception e){
            //取消
            setResult(RESULT_CANCELED);
            return false;
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
    public void loginByPost(String userName, String userPass, String enableMacAuth) {

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
            final String result_print;
            if(status == 1){
                result_print = "一键登录 | "
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

            OnetouchActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OnetouchActivity.this, result_print, Toast.LENGTH_LONG).show();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
