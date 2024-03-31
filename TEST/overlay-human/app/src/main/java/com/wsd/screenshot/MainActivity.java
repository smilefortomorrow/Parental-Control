package com.wsd.screenshot;

import static android.os.Build.VERSION_CODES.BASE;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wsd.screenshot.activity.ReqScreenCaptureActivity;
import com.wsd.screenshot.service.ScreenCaptureService;

public class MainActivity extends AppCompatActivity {

    public final int REQUEST_CODE_CAPTURE = 1001;
    public final int REQUEST_CODE_OVERLAY = 1002;

    private Intent IntentCapture = null;
    private boolean enableOverlay = false;

    public View btnPrivCapture;
    public View btnPrivOverlay;
    public View btnStart;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPrivCapture = findViewById(R.id.btnPrivCapture);
        btnPrivOverlay = findViewById(R.id.btnPrivOverlay);
        btnStart = findViewById(R.id.btnStart);

        if (Settings.canDrawOverlays(this)) {
            enableOverlay = true;
        }

        updateButtonColor();
    }

    public void OnBtnPrivCapture(View v){
        if (IntentCapture != null) return;

        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // Start activity that will return result for the screen capture permission request
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_CAPTURE);
    }
    public void OnBtnPrivOverylay(View v){
        String strPackage = "package:" + getPackageName();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse(strPackage));
        /* request permission via start activity for result */
        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
    }
    public void OnBtnStart(View v){
        if (enableOverlay == false || IntentCapture == null) return;
        Intent serviceIntent = new Intent(getApplicationContext(), ScreenCaptureService.class);
        serviceIntent.putExtra("resultCode", RESULT_OK);
        serviceIntent.putExtra("data", IntentCapture);
        startForegroundService(serviceIntent);
    }

    private void updateButtonColor() {
        if (IntentCapture != null){
            btnPrivCapture.setBackgroundColor(Color.GREEN);
        }
        if (enableOverlay){
            btnPrivOverlay.setBackgroundColor(Color.GREEN);
        }
        if (enableOverlay && IntentCapture != null){
            btnStart.setBackgroundColor(Color.GREEN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                IntentCapture = data;
            }
            updateButtonColor();
        }else if (requestCode == REQUEST_CODE_OVERLAY){
            if (Settings.canDrawOverlays(this)) {
                enableOverlay = true;
            }
//            if (resultCode == RESULT_OK && data != null) {
//                enableOverlay = true;
//            }
            updateButtonColor();
        }
    }

    public void moveToNextPage(){
        Intent intent = new Intent(MainActivity.this, ReqScreenCaptureActivity.class);
        startActivity(intent);
    }


}