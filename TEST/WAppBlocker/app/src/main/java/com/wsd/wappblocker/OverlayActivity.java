package com.wsd.wappblocker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class OverlayActivity extends AppCompatActivity {

    private static final String TAG = "Overlay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);
        if (checkDrawOverlayPermission()) {
            startOverlayService();
        }
    }

    private static final int REQUEST_CODE = 1;
    private boolean checkDrawOverlayPermission() {
        /* check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "canDrawOverlays NOK");
            /* if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /* request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            Log.d(TAG, "canDrawOverlays OK");
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /* check if received result code
         is equal our requested code for draw permission  */
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startOverlayService();
            }
        }
    }

    private void startOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        startForegroundService(intent);
    }
}