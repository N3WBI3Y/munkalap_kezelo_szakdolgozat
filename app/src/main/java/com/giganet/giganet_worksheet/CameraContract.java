package com.giganet.giganet_worksheet;

import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;

import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;

import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;

import org.greenrobot.eventbus.Subscribe;

public interface CameraContract {
    interface Model {
        float getZoomAmount();

        void setZoomAmount(int progress);

        int getLensFacing();

        int getFlashMode();

        int getNumberOfPicture();

        int getMaxNumberOfPicture();

        float getLocationAccuracy();

        void setLocationAccuracy(float accuracy);

        Camera getCamera();

        void setCamera(androidx.camera.core.Camera camera);

        ImageCapture getImageCapture();

        void setImageCapture(ImageCapture imageCapture);

        ServiceConnection getConnection();

        LocationService getLocationService();

        void setFlashMode();

        void switchCamera();

        void takePhoto(Context context);

        boolean canImageTaken();

    }

    interface Presenter {
        float getZoomAmount();

        void setZoomAmount(int progress);

        int getLensFacing();

        int getFlashMode();

        Camera getCamera();

        void setCamera(Camera camera);

        ImageCapture getImageCapture();

        void setImageCapture(ImageCapture imageCapture);

        void bindService();

        void unbindService();

        void setFlashMode();

        void switchCamera();

        void setNumberOfPicture();

        boolean takePhoto();

        @Subscribe
        void onNumberOfPictureChange(NumberOfPictureChangeEvent event);
    }

    interface View {
        Activity getActivity();

        void setLocationAccuracy(float accuracy);

        void setFlashMode(int flashMode);

        void setCamera();

        void switchCamera();

        void switchFlashMode();

        void setNumberOfPicture(int numberOfPicture);

        void exceedMaxPicture(int maxNumberOfPicture);
    }
}
