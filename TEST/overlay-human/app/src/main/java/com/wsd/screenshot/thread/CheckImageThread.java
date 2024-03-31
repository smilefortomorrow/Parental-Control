package com.wsd.screenshot.thread;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.wsd.ai.Classifier;
import com.wsd.ai.Detector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CheckImageThread extends Thread implements Runnable {
    private final String TAG = "ScreenCaptureService";
    private ImageReader imgReader;
    private boolean running = true;
    private long before;
    private Handler _handler;
    private Detector detector;
    private IListenerOnCapture listenerOnCapture;

    public interface IListenerOnCapture{
        public void OnBefore();
        public void OnAfter();
        public void OnCapture(List<Classifier.Recognition> list_detected);
    }

    public CheckImageThread(Handler handler, ImageReader imageReader, int w, int h, Context context, IListenerOnCapture pListener) {
        imgReader = imageReader;
        _handler = handler;
        detector = new Detector(context, w, h);
        listenerOnCapture = pListener;
    }

    @Override
    public void run() {
        if (listenerOnCapture != null){
            listenerOnCapture.OnBefore();
        }
        Image image = prepareImageReader();
        if (image == null){
            if (listenerOnCapture != null){
                listenerOnCapture.OnAfter();
            }
            if (running){
                _handler.postDelayed(this, 300);
            }
            return;
        }
        if (listenerOnCapture != null){
            listenerOnCapture.OnAfter();
        }
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * imgReader.getWidth();
        int width = (imgReader.getWidth() + rowPadding / pixelStride);
        int height = imgReader.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();


        try{
            if (detector != null) {
                List<Classifier.Recognition> l = detector.detectObjects(bitmap);
                if (listenerOnCapture != null){
                    listenerOnCapture.OnCapture(l);
                }
            }
        }catch (Exception ex){

        }

        if (running){
            _handler.postDelayed(this, 300);
        }
    }

    public void stopThread() {
        this.running = false;
    }

    private Image prepareImageReader() {
        try{
            return imgReader.acquireLatestImage();
        }catch (Exception ex){
            return null;
        }
    }
}
