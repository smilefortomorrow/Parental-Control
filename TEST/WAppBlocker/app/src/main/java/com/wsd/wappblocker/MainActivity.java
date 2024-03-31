package com.wsd.wappblocker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
        MyReceiver myReceiver = new MyReceiver();
        IntentFilter inf = new IntentFilter();
        registerReceiver(myReceiver, inf);
        sendBroadcast(intent);
        alertDi("Priviledge", "This program need administrator priviledge.", Settings.ACTION_ACCESSIBILITY_SETTINGS);

        findViewById(R.id.buttonOverlay).setOnClickListener(v->{
            Intent intentOverlay = new Intent(MainActivity.this, OverlayActivity.class);
            startActivity(intentOverlay);
        });
    }

    public void alertDi(String Title, String msg, final String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(Title);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(action);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}