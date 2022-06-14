package com.giganet.giganet_worksheet.Presenter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;

import com.giganet.giganet_worksheet.CameraContract;
import com.giganet.giganet_worksheet.Model.CameraModel;
import com.giganet.giganet_worksheet.Resources.Enums.WorkflowTypes;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

public class CameraPresenter implements CameraContract.Presenter {
    private final CameraContract.Model model;
    private final CameraContract.View view;
    private final Timer locationTimer;


    public CameraPresenter(CameraContract.View view, int workId, String pictureType, int maxNumberOfPicture
            , WorkflowTypes workflowType, int lensFacing, int flashMode, float zoomAmount, String filePath) {
        this.view = view;
        this.model = new CameraModel(zoomAmount, lensFacing, flashMode, filePath, pictureType
                , workflowType, workId, maxNumberOfPicture);
        locationTimer = new Timer();
        locationTimer.schedule(new CheckAccuracy(model.getLocationService()), 1000, 5 * 1000);
    }

    @Override
    public float getZoomAmount() {
        return model.getZoomAmount();
    }

    @Override
    public void setZoomAmount(int progress) {
        model.setZoomAmount(progress);
    }

    @Override
    public int getLensFacing() {
        return model.getLensFacing();
    }

    @Override
    public int getFlashMode() {
        return model.getFlashMode();
    }

    @Override
    public Camera getCamera() {
        return model.getCamera();
    }

    @Override
    public void setCamera(Camera camera) {
        model.setCamera(camera);
    }

    @Override
    public ImageCapture getImageCapture() {
        return model.getImageCapture();
    }

    @Override
    public void setImageCapture(ImageCapture imageCapture) {
        model.setImageCapture(imageCapture);
    }

    @Override
    public void bindService() {
        Intent bindIntent = new Intent(view.getActivity(), LocationService.class);
        view.getActivity().bindService(bindIntent, model.getConnection(), Context.BIND_AUTO_CREATE);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void unbindService() {
        view.getActivity().unbindService(model.getConnection());
        locationTimer.cancel();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void setFlashMode() {
        model.setFlashMode();
        view.setFlashMode(model.getFlashMode());
    }

    @Override
    public void switchCamera() {
        model.switchCamera();
        view.setCamera();
    }

    @Override
    public void setNumberOfPicture() {
        view.setNumberOfPicture(model.getNumberOfPicture());
    }

    @Override
    public boolean takePhoto() {
        if (model.canImageTaken ()) {
            model.takePhoto(view.getActivity());
            return true;
        }
        view.exceedMaxPicture(model.getMaxNumberOfPicture());
        return false;
    }

    @Override
    @Subscribe
    public void onNumberOfPictureChange(NumberOfPictureChangeEvent event) {
        setNumberOfPicture();
    }


    private class CheckAccuracy extends TimerTask {
        final LocationService locationService;

        private CheckAccuracy(LocationService locationService) {
            this.locationService = locationService;
        }

        @Override
        public void run() {
            model.getLocationService().getCurrentLocation().addOnCompleteListener(task -> {
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            model.setLocationAccuracy(location.getAccuracy());
                            view.setLocationAccuracy(model.getLocationAccuracy());
                        }
                    }
                });

                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        model.setLocationAccuracy(0.0f);
                        view.setLocationAccuracy(model.getLocationAccuracy());
                    }
                });
            });
        }
    }
}
