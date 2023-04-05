package com.unitral.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.unitral.finalproject.databinding.ActivityCameraBinding;
import com.unitral.finalproject.image_uri.Image_Uri;
import com.unitral.finalproject.model.Ml_Models;

import java.io.File;
import java.util.concurrent.Executors;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    final int Time_Limit_For_Camera_Flip_in_ms = 300;
    private final String TAG = "CAMERA";
    ActivityCameraBinding cameraBinding;
    ImageButton button, flash;
    TextView logtext;
    PreviewView previewView;
    ScaleGestureDetector scaleGestureDetector = null;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    boolean cam = false;
    Ml_Models model = null;
    long lastime = 0;
    String current;
    Camera cam1;
    long CF_Last_Tuch = 0;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        model = new Ml_Models(getApplicationContext());

        findViewById(R.id.capture).setOnClickListener(this);
        previewView = findViewById(R.id.viewFinder);
        logtext = findViewById(R.id.logtext);
        flash = findViewById(R.id.flash);
        flash.setOnClickListener(this);
        logtext.setOnClickListener(this);


        scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), getOnScaleGestureDetector());
        previewView.setOnTouchListener(this::OnPreviewTouch);//((view, event) -> OnPreviewTouch(view,event))

        //camera permission
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted)
                        openCamera();
                    else
                        Toast.makeText(getApplicationContext(), "camera ki Permission nhi hain", Toast.LENGTH_LONG).show();
                });
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);


        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        Executors.newSingleThreadExecutor();

    }

    private boolean OnPreviewTouch(View view, @NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (System.currentTimeMillis() - CF_Last_Tuch < Time_Limit_For_Camera_Flip_in_ms)
                    openCamera();
                CF_Last_Tuch = System.currentTimeMillis();

                break;
            }
            case MotionEvent.ACTION_UP: {
                MeteringPointFactory pointFactory = new SurfaceOrientedMeteringPointFactory(
                        previewView.getWidth(), previewView.getHeight()
                );
                MeteringPoint autoFocusPoint = pointFactory.createPoint(event.getX(), event.getY());
                try {
                    cam1.getCameraControl().startFocusAndMetering(
                            new FocusMeteringAction.Builder(
                                    autoFocusPoint,
                                    FocusMeteringAction.FLAG_AF
                            ).build()
                    );
                } catch (Exception ignored) {
                }
            }
            break;
        }
        return scaleGestureDetector.onTouchEvent(event);
    }


    private ScaleGestureDetector.OnScaleGestureListener getOnScaleGestureDetector() {
        return new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                ZoomState f = cam1.getCameraInfo().getZoomState().getValue();
                assert f != null;
                Log.d("Zoom", String.valueOf(f.getZoomRatio()));

                float scale = scaleGestureDetector.getScaleFactor();
                cam1.getCameraControl().setZoomRatio(scale * f.getZoomRatio());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            }
        };
    }

    //done
    private void startAnalyzer() {
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                Bitmap bitmap = previewView.getBitmap();

                image.close();

                if (bitmap == null) return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - lastime > 2000) {
                            lastime = System.currentTimeMillis();
                            logtext.setText(setImageWithUri(bitmap));
                        }

                    }
                });
            }
        });
    }

    //done
    private void openCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (Exception e) {
            }
        }, ContextCompat.getMainExecutor(this));
    }


    //done
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        cam = !cam;
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //image capture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        cam1 = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, imageAnalysis, preview);
    }

    //for clicking the pick
    private void takePicture() {
        long Timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, Timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {

                        Image_Uri.setFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                "/" + Timestamp + ".jpg"));
                        finish();
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.d("Error", error.fillInStackTrace().toString());
                        Toast.makeText(CameraActivity.this, error.fillInStackTrace().toString(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    //done
    private String setImageWithUri(Bitmap bitmap) {

        try {
            if (null != bitmap) {
                int dimention = Math.min(bitmap.getHeight(), bitmap.getWidth());
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimention, dimention);

                model.setBitmap(bitmap);
                return model.Object_Detection();
            }
        } catch (Exception ignored) {

        }
        return "Not";

    }

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.flash: {
                if (cam1.getCameraInfo().hasFlashUnit()) {
                    cam = !cam;
                    cam1.getCameraControl().enableTorch(cam);
                    flash.setColorFilter(cam ? Color.YELLOW : Color.WHITE);
                }
                break;
            }
            case R.id.capture: {
                takePicture();
                break;
            }
            case R.id.logtext: {
                cam = !cam;
                if (cam) {
                    imageAnalysis.clearAnalyzer();
                    logtext.setText("Deactivated");
                } else
                    startAnalyzer();
                break;
            }
            default: {
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        imageAnalysis.clearAnalyzer();

    }
}