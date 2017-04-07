package com.demo.hearyshen.sudalogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


public class BootReceiver extends BroadcastReceiver {

    private static final String TAG="BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
        SharedPreferences prefs = context.getSharedPreferences("SudaLoginDev", Context.MODE_PRIVATE);

        if( prefs.getBoolean("enableBootLogin", false) ) {
            Toast.makeText(context, "从BootCompleted启动", Toast.LENGTH_SHORT).show();
            Intent mainintent = new Intent(context, MainActivity.class);
            context.startActivity(mainintent);
        }
    }

}
