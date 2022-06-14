package com.giganet.giganet_worksheet.View;

import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.CameraService.DEFAULT_HEIGHT;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.CameraService.DEFAULT_WIDTH;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.giganet_worksheet.R;
import com.example.giganet_worksheet.databinding.ActivityCameraViewBinding;
import com.giganet.giganet_worksheet.CameraContract;
import com.giganet.giganet_worksheet.Presenter.CameraPresenter;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.Resources.Enums.WorkflowTypes;
import com.giganet.giganet_worksheet.Utils.PermissionContainer;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraViewActivity extends AppCompatActivity implements CameraContract.View, SeekBar.OnSeekBarChangeListener
        , View.OnTouchListener {

    private ActivityCameraViewBinding viewBinding;
    private CameraContract.Presenter presenter;
    private ScaleGestureDetector scaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraViewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        Intent dataIntent = getIntent();
        if (dataIntent == null || dataIntent.getExtras() == null) finish();
        init(dataIntent.getExtras(), savedInstanceState);
        setListeners();
        if (PermissionContainer.CameraPermission.checkPermission(this)) {
            startCamera();
        } else {
            PermissionContainer.CameraPermission.askPermission(this);
        }
        presenter.bindService();

    }

    private void init(Bundle intentData, Bundle savedInstance) {
        String pictureType = intentData.getString("workState");
        int workId = intentData.getInt("workId", -1);
        int maxNumberOfPicture = intentData.getInt("maxNumberOfPicture", 1000);
        WorkflowTypes workflowType = pictureType.equals(WorkState.DOCUMENTATION.toString()) ? WorkflowTypes.DOCUMENTATION : WorkflowTypes.INSTALLATION;
        String filePath = intentData.getString("filePath");

        int lensFacing;
        int flashMode;
        float zoomAmount;
        if (savedInstance != null) {
            lensFacing = savedInstance.getInt("orientation", 0);
            flashMode = savedInstance.getInt("flashMode", ImageCapture.FLASH_MODE_AUTO);
            zoomAmount = savedInstance.getFloat("zoomAmount", 1.0f);

        } else {
            lensFacing = 1;
            flashMode = ImageCapture.FLASH_MODE_AUTO;
            zoomAmount = 1.0f;
        }
        presenter = new CameraPresenter(this, workId, pictureType, maxNumberOfPicture, workflowType
                , lensFacing, flashMode, zoomAmount, filePath);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        setFlashMode(presenter.getFlashMode());
        viewBinding.sbZoomBar.setMin(1);
        presenter.setNumberOfPicture();
    }

    private void setListeners() {
        viewBinding.ibCapturePhoto.setOnClickListener(v -> {
            takePhoto();
        });

        viewBinding.ibSwitchCamera.setOnClickListener(v -> {
            switchCamera();
        });

        viewBinding.ibSwitchFlashMode.setOnClickListener(v -> {
            switchFlashMode();
        });

        viewBinding.ibBackButton.setOnClickListener(v -> {
            finish();
        });

        viewBinding.sbZoomBar.setOnSeekBarChangeListener(this);
        viewBinding.tvImagePreview.setOnTouchListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unbindService();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean finalResult = false;
        for (int result : grantResults) {
            finalResult |= result == RESULT_OK;
        }
        if (finalResult) {
            finish();
        } else {
            startCamera();
        }
    }

    @Override
    public void switchFlashMode() {
        presenter.setFlashMode();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("orientation", presenter.getLensFacing());
        outState.putInt("flashMode", presenter.getFlashMode());
        outState.putFloat("zoomAmount", presenter.getZoomAmount());
    }

    @Override
    public void setLocationAccuracy(float accuracy) {
        viewBinding.iwGpsAccuracy.setBackground(getAccuracyImage(accuracy));
    }

    public void setFlashMode(int flashMode) {
        viewBinding.ibSwitchFlashMode.setBackground(getCameraFlashImage(flashMode));
    }

    @Override
    public void setCamera() {
        startCamera();
    }

    @Override
    public void switchCamera() {
        presenter.switchCamera();
    }

    private void takePhoto() {
        if (presenter.takePhoto()) {
            imageCaptureFlash(viewBinding.iwImageCaptureFlash);
        }

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider provider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewBinding.tvImagePreview.getSurfaceProvider());
                    int orientation = getResources().getConfiguration().orientation;
                    Size targetResolution = orientation == 1 ? new Size(DEFAULT_HEIGHT, DEFAULT_WIDTH) : new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    presenter.setImageCapture(new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setTargetResolution(targetResolution).build());

                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(presenter.getLensFacing()).build();
                    viewBinding.sbZoomBar.setProgress(1);
                    provider.unbindAll();
                    presenter.setCamera(provider.bindToLifecycle(CameraViewActivity.this, cameraSelector
                            , preview, presenter.getImageCapture()));

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    public void setNumberOfPicture(int counter) {
        viewBinding.twNumberOfPictures.setText(String.format("Képek száma : %d", counter));
    }

    @Override
    public void exceedMaxPicture(int maxNumberOfPicture) {
        Toast.makeText(this, "Csak " + maxNumberOfPicture + " kép készíthető a munkafolyamathoz", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //https://developer.android.com/reference/androidx/camera/core/CameraControl#startFocusAndMetering(androidx.camera.core.FocusMeteringAction)
        scaleGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            MeteringPointFactory factory = viewBinding.tvImagePreview.getMeteringPointFactory();
            MeteringPoint point = factory.createPoint(event.getX(), event.getY());
            FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();

            presenter.getCamera().getCameraControl().startFocusAndMetering(action);
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (presenter.getCamera() != null) {
            presenter.setZoomAmount(seekBar.getProgress());
            presenter.getCamera().getCameraControl().setLinearZoom(presenter.getZoomAmount() / 100.0f);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}


    @Override
    public Activity getActivity() {
        return this;
    }


    private Drawable getAccuracyImage(float accuracy) {
        if (accuracy <= 0.5f) {
            Drawable normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.gps_accuracy_low, null);
            return normalImage;
        } else if (accuracy <= 15.0f) {
            Drawable normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.gps_accuracy_high, null);
            return normalImage;
        } else if (accuracy <= 35.0f) {
            Drawable normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.gps_accuracy_mid, null);
            return normalImage;
        } else {
            Drawable normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.gps_accuracy_low, null);
            return normalImage;
        }
    }

    private Drawable getCameraFlashImage(int flashMode) {
        Drawable normalImage;
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.camera_flash_off, null);
                break;
            case ImageCapture.FLASH_MODE_ON:
                normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.camera_flash_on, null);
                break;
            default:
                normalImage = ResourcesCompat.getDrawable(getResources(), R.drawable.camera_flash_auto, null);
                break;
        }

        return normalImage;
    }

    private void imageCaptureFlash(final ImageView v) {

        v.setImageAlpha(0);
        v.setVisibility(View.VISIBLE);

        Animation fadeIn = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime,
                                               Transformation t) {

                v.setVisibility(View.VISIBLE);
                int new_alpha = (int) (0 + (interpolatedTime * 255));
                v.setImageAlpha(new_alpha);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        Animation fadeOut = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime,
                                               Transformation t) {

                v.setVisibility(View.VISIBLE);
                int new_alpha = (int) (255 - (interpolatedTime * 255));
                v.setImageAlpha(new_alpha);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        fadeIn.setDuration(300);
        fadeOut.setDuration(300);
        v.startAnimation(fadeIn);
        v.postDelayed(() -> {
            v.startAnimation(fadeOut);
            v.setVisibility(View.GONE);
        }, 300);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //https://medium.com/quick-code/pinch-to-zoom-with-multi-touch-gestures-in-android-d6392e4bf52d
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            if (presenter.getCamera() != null) {
                float currentZoomAmount = scaleGestureDetector.getScaleFactor() > 1 ? Math.max(presenter.getZoomAmount(),25) : presenter.getZoomAmount();
                currentZoomAmount *= scaleGestureDetector.getScaleFactor();
                currentZoomAmount = Math.max(1.0f,Math.min(currentZoomAmount, 100.0f));
                viewBinding.sbZoomBar.setProgress((int) currentZoomAmount);
                presenter.setZoomAmount((int) currentZoomAmount);
                presenter.getCamera().getCameraControl().setLinearZoom(presenter.getZoomAmount() / 100.0f);
            }
            return true;
        }
    }

}