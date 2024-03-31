package com.wsd.wappblocker;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class OverlayService extends Service implements OnTouchListener, OnClickListener {
    private static String TAG = "OverlayService";
    private WindowManager wm;
    private Button button;
    private static Timer timer = new Timer();
    private static OverlayService pThis;
    private WindowManager.LayoutParams layoutPosition;

    @SuppressLint({"ForegroundServiceType", "SetTextI18n"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onCreate();

        pThis = this;
        String CHANNEL_ID = "channel1";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Overlay notification",
                NotificationManager.IMPORTANCE_LOW);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("adsf")
                .setContentText("asdf1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
/*
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        button = new Button(pThis);
        button.setBackgroundResource(R.drawable.ic_launcher_background);
        button.setText("Button");
        button.setAlpha(1);
        button.setBackgroundColor(Color.BLUE);
        button.setOnClickListener(pThis);

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        layoutPosition = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        layoutPosition.gravity = Gravity.START | Gravity.TOP;
        layoutPosition.x = 0;
        layoutPosition.y = 0;
        wm.addView(button, layoutPosition);

        handler.post(moveViewRunnable);
*/
        return START_NOT_STICKY;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int deltaX = 10;
    private final Runnable moveViewRunnable = new Runnable() {
        @Override
        public void run() {
/*
	            // Update the overlay view's X position
            layoutPosition.x += deltaX;

            // Apply the updated layout parameters to the window manager
            wm.updateViewLayout(button, layoutPosition);

            // Reschedule the next execution
            handler.postDelayed(this, 1000);
*/
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, " ++++ On touch");
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, " ++++ On click");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (button != null) {
            wm.removeView(button);
            button = null;
        }
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

}