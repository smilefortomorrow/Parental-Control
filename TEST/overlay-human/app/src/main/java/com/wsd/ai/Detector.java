package com.wsd.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Detector {

    // Configuration values for the prepackaged SSD model.
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
//    private static final int TF_OD_API_INPUT_SIZE = 448;
//    private static final String TF_OD_API_MODEL_FILE = "224.tflite";
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.7f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    private final int previewWidth;
    private final int previewHeight;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private byte[] luminanceCopy;

    private BorderedText borderedText;

    Context ctx;

    public Detector(Context context, int w, int h) {
//        final float textSizePx =
//                TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
//        borderedText = new BorderedText(textSizePx);
//        borderedText.setTypeface(Typeface.MONOSPACE);

        ctx = context;
        tracker = new MultiBoxTracker(context);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            context.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
//            LOGGER.e("Exception initializing classifier!", e);
        }

        previewWidth = w;
        previewHeight = h;

        sensorOrientation = 0;//rotation - getScreenOrientation();
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
//
//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

//        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
//        trackingOverlay.addCallback(
//                new OverlayView.DrawCallback() {
//                    @Override
//                    public void drawCallback(final Canvas canvas) {
//                        tracker.draw(canvas);
//                        if (isDebug()) {
//                            tracker.drawDebug(canvas);
//                        }
//                    }
//                });

    }

    public List<Classifier.Recognition> detectObjects(Bitmap bitmap){
        ++timestamp;
        final long currTimestamp = timestamp;

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        {

    //      LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas1 = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
                case TF_OD_API:
                    minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    break;
            }

            final List<Classifier.Recognition> mappedRecognitions =
                    new LinkedList<Classifier.Recognition>();

            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= minimumConfidence) {
                    canvas1.drawRect(location, paint);

                    cropToFrameTransform.mapRect(location);

                    result.setLocation(location);
                    mappedRecognitions.add(result);
                    Log.d("Test", result.getTitle());
                    //Log.d("Dist", "Distance: " + result.getLocation().height());
                    //getDistance(result.getLocation());
                    //speakDetectedObject(result.getTitle());
                    //Thread.sleep(2000);
                    Log.w("aaaa",  "aaaaaaaaaaaaaaaaaaaaaaaaaa" + result.toString());

                }
            }

            computingDetection = false;
            return mappedRecognitions;

//            tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
//            trackingOverlay.postInvalidate();
//            ctx.runOnUiThread(
//                    () -> {
//                        showFrameInfo(previewWidth + "x" + previewHeight);
//                        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
//                        showInference(lastProcessingTimeMs + "ms");
//                    });
        }
    }

    private enum DetectorMode {
        TF_OD_API
    }

    void getDistance(RectF location){
//        double focalLength;
//        double imageHieght = Math.round((location.top - location.bottom) * 0.0264583333 *10)/10.0; // image height in centimeters
//        if (isUseCamera2API()){
//            focalLength = ((CameraConnectionFragment)getFragmentManager().findFragmentById(R.id.container)).getFocalLength();
//        }
//        else{
//            focalLength = ((LegacyCameraConnectionFragment)getFragmentManager().findFragmentById(R.id.container)).getFocalLength();
//        }
//        Log.d("Dis", "distance: "+ Math.round(focalLength*1500/imageHieght)/10.0);
//        Toast.makeText(this, "distance: "+ Math.round(focalLength*1500/imageHieght)/10.0, Toast.LENGTH_SHORT).show();
    }

}
