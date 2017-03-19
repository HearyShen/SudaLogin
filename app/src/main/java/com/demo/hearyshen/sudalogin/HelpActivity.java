package com.demo.hearyshen.sudalogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HelpActivity extends AppCompatActivity {

    private Switch sw_smartLogin, sw_autoSave;
    private TextView tv_smartLogin, tv_autoSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        sw_autoSave = (Switch) findViewById(R.id.sw_autoSave);
        tv_autoSave = (TextView) findViewById(R.id.tv_autoSave);
        sw_smartLogin = (Switch) findViewById(R.id.sw_smartLogin);
        tv_smartLogin = (TextView) findViewById(R.id.tv_smartLogin);

        //获得SharedPreferences对象
        final SharedPreferences preferences = HelpActivity.this.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        sw_autoSave.setChecked( preferences.getBoolean("enableAutoSave",true) );   // 默认开启自动记忆
        tv_autoSave.setText(sw_autoSave.isChecked()?R.string.autoSave_tips_on:R.string.autoSave_tips_off);

        sw_autoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked)
                {
                    editor.putBoolean("enableAutoSave", true);

                    sw_smartLogin.setEnabled(true);
                }
                else
                {
                    editor.putBoolean("enableAutoSave", false);

                    editor.putString("username", null);
                    editor.putString("password", null);
                    editor.putString("enableMacAuth","0");
                    editor.putInt("frequency", 0);

                    editor.putBoolean("enableSmartLogin", false);
                    sw_smartLogin.setChecked(false);
                    sw_smartLogin.setEnabled(false);
                }
                editor.apply();
                tv_autoSave.setText(sw_autoSave.isChecked()?R.string.autoSave_tips_on:R.string.autoSave_tips_off);

                // 更新智能登陆相关显示
                sw_smartLogin.setChecked( preferences.getBoolean("enableSmartLogin",true) );   // 默认开启智能登陆
                String freq_count_str = "已为您自动登陆："+preferences.getInt("autoFrequency",0)+"次\n";
                final String smartLogin_on_str = freq_count_str.concat(getResources().getString(R.string.smartLogin_tips_on));
                tv_smartLogin.setText(sw_smartLogin.isChecked()?smartLogin_on_str:getString(R.string.smartLogin_tips_off));
            }
        });

        sw_smartLogin.setChecked( preferences.getBoolean("enableSmartLogin",true) );   // 默认开启智能登陆
        String freq_count_str = "已为您自动登陆："+preferences.getInt("autoFrequency",0)+"次\n";
        final String smartLogin_on_str = freq_count_str.concat(getResources().getString(R.string.smartLogin_tips_on));
        tv_smartLogin.setText(sw_smartLogin.isChecked()?smartLogin_on_str:getString(R.string.smartLogin_tips_off));

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
                tv_smartLogin.setText(sw_smartLogin.isChecked()?smartLogin_on_str:getString(R.string.smartLogin_tips_off));
            }
        });

        if( !preferences.getBoolean("enableAutoSave", true) ){
            sw_smartLogin.setChecked(false);
            sw_smartLogin.setEnabled(false);
            tv_smartLogin.setText(sw_smartLogin.isChecked()?smartLogin_on_str:getString(R.string.smartLogin_tips_off));
        }
    }
}
