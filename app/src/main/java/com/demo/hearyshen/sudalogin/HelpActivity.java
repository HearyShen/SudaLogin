package com.demo.hearyshen.sudalogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class HelpActivity extends AppCompatActivity {

    private Switch sw_smartLogin, sw_autoSave, sw_bootLogin, sw_netchangeLogin;
    private TextView tv_smartLogin, tv_bootLogin, tv_netchangeLogin, tv_autoSave, tv_oneTouch_caption;
    private Button btn_oneTouch;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        sw_autoSave = (Switch) findViewById(R.id.sw_autoSave);
        tv_autoSave = (TextView) findViewById(R.id.tv_autoSave);
        sw_smartLogin = (Switch) findViewById(R.id.sw_smartLogin);
        tv_smartLogin = (TextView) findViewById(R.id.tv_smartLogin);
        sw_bootLogin = (Switch) findViewById(R.id.sw_bootLogin);
        tv_bootLogin = (TextView) findViewById(R.id.tv_bootLogin);
        sw_netchangeLogin = (Switch) findViewById(R.id.sw_netchangeLogin);
        tv_netchangeLogin = (TextView) findViewById(R.id.tv_netchangeLogin);
        tv_oneTouch_caption = (TextView) findViewById(R.id.tv_oneTouch_caption);
        btn_oneTouch = (Button) findViewById(R.id.btn_oneTouch);

        //获得SharedPreferences对象
        preferences = HelpActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        // 通过runOnUiThread方法进行修改主线程的控件内容
        HelpActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        });

        /*
        * 自动保存开关事件处理
        * */
        sw_autoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked)
                {
                    editor.putBoolean("enableAutoSave", true);
                }
                else
                {
                    editor.clear();

                    editor.putBoolean("enableAutoSave", false);
                    editor.putBoolean("enableSmartLogin", false);
                }
                editor.apply();

                // 通过runOnUiThread方法进行修改主线程的控件内容
                HelpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        });


        /*
        * 智能登陆开关事件处理
        * */
        sw_smartLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked)
                {
                    editor.putBoolean("enableSmartLogin", true);
                }
                else
                {
                    editor.putBoolean("enableSmartLogin", false);
                }
                editor.apply();

                // 通过runOnUiThread方法进行修改主线程的控件内容
                HelpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        });

        /*
        * 智能登陆 - 开机启动开关事件处理
        * */
        sw_bootLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked)
                {
                    editor.putBoolean("enableBootLogin", true);
                }
                else
                {
                    editor.putBoolean("enableBootLogin", false);
                }
                editor.apply();

                // 通过runOnUiThread方法进行修改主线程的控件内容
                HelpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        });

        /*
        * 智能登陆 - 网络感知开关事件处理
        * */
        sw_netchangeLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked)
                {
                    editor.putBoolean("enableNetChangeLogin", true);
                }
                else
                {
                    editor.putBoolean("enableNetChangeLogin", false);
                }
                editor.apply();

                // 通过runOnUiThread方法进行修改主线程的控件内容
                HelpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        });

        /*
        * 一键登陆开关事件处理
        * */
        btn_oneTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建快捷方式的Intent
                Intent installShortcut= new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                //不允许重复创建//名称 //图标//点击快捷图片，运行的程序主入口
                installShortcut.putExtra("duplicate", false);
                installShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,getString(R.string.oneTouchLogin_title));

                installShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(HelpActivity.this, R.mipmap.onetouch_icon));

                installShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(HelpActivity.this , OnetouchActivity.class));
                //发送广播
                sendBroadcast(installShortcut);
                Toast.makeText(HelpActivity.this, getString(R.string.oneTouchLogin_installing), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshUI()
    {
        /*更新自动保存相关显示*/
        sw_autoSave.setChecked( preferences.getBoolean("enableAutoSave",true) );   // 默认开启自动记忆
        tv_autoSave.setText(sw_autoSave.isChecked()?R.string.autoSave_tips_on:R.string.autoSave_tips_off);

        /*更新智能登陆相关显示*/
        sw_smartLogin.setChecked( preferences.getBoolean("enableSmartLogin",true) );   // 默认开启智能登陆
        final String smartLogin_on_str = getResources().getString(R.string.smartLogin_tips_on) + "已为您自动登陆："+preferences.getInt("autoFrequency",0)+"次\n";
        tv_smartLogin.setText(sw_smartLogin.isChecked()?smartLogin_on_str:getString(R.string.smartLogin_tips_off));
        sw_smartLogin.setEnabled(preferences.getBoolean("enableAutoSave", true));

        /*更新开机自启相关显示*/
        sw_bootLogin.setChecked( preferences.getBoolean("enableBootLogin",false) );   // 默认关闭
        tv_bootLogin.setText(sw_bootLogin.isChecked()?"开机启动自动登录："+preferences.getInt("bootLoginFrequency",0)+"次\n":getString(R.string.bootLogin_tips_off));
        sw_bootLogin.setEnabled(preferences.getBoolean("enableSmartLogin", true));    // 选项启用：随智能登录启用

        /*更新网络变化自动登录相关显示*/
        sw_netchangeLogin.setChecked( preferences.getBoolean("enableNetChangeLogin",false) );   // 默认关闭
        tv_netchangeLogin.setText(sw_netchangeLogin.isChecked()?"自动处置网络变动："+preferences.getInt("netChangeLoginFrequency",0)+"次\n":getString(R.string.netchangeLogin_tips_off));
        sw_netchangeLogin.setEnabled(preferences.getBoolean("enableSmartLogin", true));    // 选项启用：随智能登录启用

        tv_oneTouch_caption.setText("一键登录已为您快速登录："+preferences.getInt("oneTouchFrequency",0)+"次\n");
    }
}
