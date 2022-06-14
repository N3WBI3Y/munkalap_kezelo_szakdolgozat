package com.giganet.giganet_worksheet.Model;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ServiceConnection;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.content.ContextCompat;

import com.giganet.giganet_worksheet.CameraContract;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.WorkflowTypes;
import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.Converters;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CameraModel implements CameraContract.Model {

    private final String filePath;
    private final String pictureType;
    private final WorkflowTypes workflowType;
    private final int workId;
    private final int maxNumberOfPicture;
    private float zoomAmount;
    private int lensFacing;
    private int flashMode;
    private float locationAccuracy;
    private int pictureCounter;
    private Camera camera;
    private ImageCapture imageCapture;
    private LocationService locationService;
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    public CameraModel(float zoomAmount, int lensFacing, int flashMode, String filePath, String pictureType, WorkflowTypes workflowType, int workId,
                       int maxNumberOfPicture) {

        this.zoomAmount = zoomAmount;
        this.lensFacing = lensFacing;
        this.flashMode = flashMode;
        this.filePath = filePath;
        this.pictureType = pictureType;
        this.workflowType = workflowType;
        this.workId = workId;
        this.maxNumberOfPicture = maxNumberOfPicture;
        countPicture();
    }

    public float getZoomAmount() {
        return zoomAmount;
    }

    @Override
    public void setZoomAmount(int progress) {
        zoomAmount = progress;
    }

    public int getLensFacing() {
        return lensFacing;
    }

    public int getFlashMode() {
        return flashMode;
    }

    public int getPictureCounter() {
        return pictureCounter;
    }

    @Override
    public int getNumberOfPicture() {
        return pictureCounter;
    }

    @Override
    public float getLocationAccuracy() {
        return locationAccuracy;
    }

    @Override
    public void setLocationAccuracy(float accuracy) {
        this.locationAccuracy = accuracy;
    }

    public int getMaxNumberOfPicture() {
        return maxNumberOfPicture;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public ImageCapture getImageCapture() {
        return imageCapture;
    }

    public void setImageCapture(ImageCapture imageCapture) {
        this.imageCapture = imageCapture;
        imageCapture.setFlashMode(flashMode);
    }

    public ServiceConnection getConnection() {
        return connection;
    }

    public LocationService getLocationService() {
        return locationService;
    }

    @Override
    public void setFlashMode() {
        flashMode = (flashMode + 1) % 3;
        if (camera != null && flashMode == ImageCapture.FLASH_MODE_ON) {
            camera.getCameraControl().enableTorch(true);
        } else {
            if (camera != null) {
                camera.getCameraControl().enableTorch(false);
            }
        }
    }

    @Override
    public void switchCamera() {
        lensFacing = lensFacing == 1 ? 0 : 1;
    }

    private void countPicture() {
        File file = new File(filePath);
        File[] files = file.listFiles();
        int counter = 0;
        for (File picture : files) {
            if (picture.getName().contains(pictureType)) {
                counter++;
            }
        }
        pictureCounter = counter;
    }

    public void takePhoto(Context context) {
        locationService.getLastLocation().addOnCompleteListener(task -> {
            task.addOnSuccessListener(location -> {
                onImageTaken(context, location.getLongitude(), location.getLatitude(), location.getTime());
            });
            task.addOnFailureListener(e -> {
                onImageTaken(context, null, null, null);
            });

        });

    }

    @Override
    public boolean canImageTaken() {
        return pictureCounter < maxNumberOfPicture;
    }

    private void onImageTaken(Context context, Double longitude, Double latitude, Long time) {
        ImageCapture imageCaptureReference = imageCapture;
        File file = new File(filePath + "/" + pictureType + "_" + System.currentTimeMillis() + ".jpeg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCaptureReference.takePicture(outputOptions, ContextCompat.getMainExecutor(context), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (workflowType.equals(WorkflowTypes.INSTALLATION)) {
                    saveToTaskImage(context, workId, longitude, latitude, file);
                } else if (workflowType.equals(WorkflowTypes.DOCUMENTATION)) {
                    saveToDocumentationImage(context, workId, file);
                }
                writeExif(file, longitude, latitude, time);
                countPicture();
                EventBus.getDefault().postSticky(new NumberOfPictureChangeEvent(pictureType, pictureCounter));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(context, "Nem sikerült a fénykép elkészítése", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void writeExif(File photoFile, Double longitude, Double latitude, Long time) {
        if (longitude == null || latitude == null || time == null) {
            return;
        }
        try {
            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude < 0.0d ? "W" : "E");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Converters.convertDoubleToGPSData(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude < 0.0d ? "S" : "N");
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Converters.convertDoubleToGPSData(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, Converters.convertTimeToGPSTime(time));
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveToTaskImage(Context context, int workId, Double longitude, Double latitude, File photoFile) {
        String username = new SharedPreference(context).getSharedPreferences().getString("USERNAME","");
        InstallationItemTableHandler db = new InstallationItemTableHandler(context);
        db.addItem(workId,username ,new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), photoFile.getAbsolutePath(),
                pictureType, longitude, latitude, null, DBStatus.CREATED.value);
    }

    private void saveToDocumentationImage(Context context, int id, File photoFile) {
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String externalIdentifier = android_id + "_" + new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis()));

        DocumentationItemTableHandler db = new DocumentationItemTableHandler(context);
        db.addItem(id, photoFile.getAbsolutePath()
                , context.getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).getString("USERNAME", ""), new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), externalIdentifier);
    }

}
