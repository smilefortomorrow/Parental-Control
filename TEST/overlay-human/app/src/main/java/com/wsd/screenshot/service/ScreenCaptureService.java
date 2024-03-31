package com.wsd.screenshot.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.wsd.ai.Classifier;
import com.wsd.screenshot.R;
import com.wsd.screenshot.activity.ReqScreenCaptureActivity;
import com.wsd.screenshot.thread.CheckImageThread;

import java.util.ArrayList;
import java.util.List;

public class ScreenCaptureService extends Service implements CheckImageThread.IListenerOnCapture {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MediaProjectionServiceChannel";

    //////////////////////////////////////////////////////////////////////////////////////////
    // for capture
    //////////////////////////////////////////////////////////////////////////////////////////
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private CheckImageThread saveImageThread;
    private int screenDensity;
    private int displayWidth;
    private int displayHeight;

    //////////////////////////////////////////////////////////////////////////////////////////
    // for overlay
    //////////////////////////////////////////////////////////////////////////////////////////
    private WindowManager wndManager;
    private List<Button> list_button;
    private List<WindowManager.LayoutParams> list_layoutParam;
    private int list_count = -1;
    private final int zoom = 3;

    Handler _handler;

    // Binder given to clients
//    private final IBinder binder = new LocalBinder();

    @Override
    public void OnBefore() {
        for (int i = 0 ; i < 10 ; i ++){
            list_button.get(i).setVisibility(View.GONE);
        }
    }

    @Override
    public void OnAfter() {
        for (int i = 0 ; i <= list_count ; i ++){
            list_button.get(i).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void OnCapture(List<Classifier.Recognition> list_detected) {
        int i = 0;
        for (i = 0 ; i < 10 && i < list_detected.size() ; i ++){
            Classifier.Recognition recognition = list_detected.get(i);
            WindowManager.LayoutParams layoutParam = list_layoutParam.get(i);
            Button btn = list_button.get(i);

            layoutParam.x = (int) recognition.getLocation().left * zoom;
            layoutParam.y = (int) recognition.getLocation().top * zoom;
            layoutParam.width = (int) recognition.getLocation().width() * zoom;
            layoutParam.height = (int) recognition.getLocation().height() * zoom;

            btn.setText(recognition.getTitle()+recognition.getConfidence().toString());

            wndManager.updateViewLayout(btn, layoutParam);
            btn.setVisibility(View.VISIBLE);
        }
        list_count = i;
        for (int j = i ; j < 10 ; j ++){
            list_button.get(j).setVisibility(View.GONE);
        }
    }

    // Class used for the client Binder.
//    public class LocalBinder extends Binder {
//        ScreenCaptureService getService() {
//            // Return this instance of ScreenCaptureService so clients can call public methods
//            return ScreenCaptureService.this;
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, ReqScreenCaptureActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("title")
                .setContentText("message")
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PrepareOverlay();
        StartCapture(intent);

        return START_NOT_STICKY;
    }

    private void PrepareOverlay() {
        wndManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        list_button = new ArrayList<>();
        list_layoutParam = new ArrayList<>();

        for (int i = 0 ; i < 10 ; i ++){
            Button btnOverlay = new Button(this);
            WindowManager.LayoutParams layoutParams;

            btnOverlay.setBackgroundResource(R.drawable.ic_launcher_background);
            btnOverlay.setText("Blocked");
            btnOverlay.setAlpha(1);
            btnOverlay.setBackgroundColor(Color.BLUE);
//            btnOverlay.setOnClickListener(pThis);
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.START | Gravity.TOP;
            layoutParams.x = 0;
            layoutParams.y = 0;
            wndManager.addView(btnOverlay, layoutParams);
            btnOverlay.setVisibility(View.GONE);
            list_button.add(btnOverlay);
            list_layoutParam.add(layoutParams);
        }
    }

    private void StartCapture(Intent intent) {
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("data");

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (resultCode != Activity.RESULT_CANCELED && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);

            // TODO: Continue with starting the image capture...
            setDeviceDisplay();
            setUpVirtualDisplay();

            _handler = new Handler();

            _handler.postDelayed(
                    new CheckImageThread(
                            _handler,
                            imageReader,
                            displayWidth,
                            displayHeight,
                            ScreenCaptureService.this,
                            this
                    ), 300);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setDeviceDisplay() {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenDensity = displayMetrics.densityDpi / zoom;
        displayWidth = displayMetrics.widthPixels / zoom;
        displayHeight = displayMetrics.heightPixels / zoom;
    }

    private void setUpVirtualDisplay() {
//        imageReader = ImageReader.newInstance(
//                displayWidth, displayHeight, ImageFormat.YUV_420_888, 2);
        imageReader = ImageReader.newInstance(
                displayWidth, displayHeight, PixelFormat.RGBA_8888, 2);

        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                displayWidth, displayHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
    }
}
