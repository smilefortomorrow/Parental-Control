package com.wsd.screenshot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wsd.screenshot.service.ScreenCaptureService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReqScreenCaptureActivity extends AppCompatActivity{
    private static final int REQUEST_CODE = 100; // Request code for capturing screen

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This creates an intent that shows UI for user to start the screen capture.
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // Start activity that will return result for the screen capture permission request
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("data", data);

                startForegroundService(serviceIntent);
            }
            finish();
        }
    }

    private void takeScreenshot() {
        // Get root view
        View rootView = getWindow().getDecorView().getRootView();

        // Create bitmap with the same size as the view
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        // Save the bitmap to file
        String filePath = Environment.getExternalStorageDirectory() + "/screenshot_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(filePath);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            // Log success or open/share file
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }


}
