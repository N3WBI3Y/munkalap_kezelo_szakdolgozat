package com.giganet.giganet_worksheet.Model;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.giganet.giganet_worksheet.DrawContract;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.PaintView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DrawModel implements DrawContract.Model {

    private final File filepath;
    private final String type;
    private final int workId;
    private  LocationService locationService;
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

    public DrawModel(File filepath, String type, int workId) {
        this.filepath = filepath;
        this.type = type;
        this.workId = workId;
    }

    public ServiceConnection getConnection() {
        return connection;
    }

    public CompletableFuture<Boolean> saveDrawing(Context context, PaintView paintView){
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Bitmap signature = paintView.getDrawBitmap();
        String timestamp = new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000));
        File output = new File(filepath + "/" + type + ".jpeg");

        if (locationService.getLastLocation() == null) {
            locationService.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    task.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (writeToFile(signature, output)) {
                                if (writeToDb(output, timestamp, location, type,context)) {
                                    result.complete(true);
                                }
                            }
                        }
                    });
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Nézd meg az a helymeghatározód", Toast.LENGTH_SHORT).show();
                            result.complete(false);
                        }
                    });
                }
            });

        } else {
            if (writeToFile(signature, output)) {
                locationService.getCurrentLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (writeToDb(output, timestamp, location, type,context)) {
                            result.complete(true);
                        }
                    }
                });
            }
        }
        return result;
    }

    @Override
    public File getFilePath() {
        return filepath;
    }

    private boolean writeToFile(Bitmap signature, File output) {
        try (FileOutputStream out = new FileOutputStream(output)) {
            return signature.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean writeToDb(File output, String timestamp, Location location, String type, Context context) {
        InstallationItemTableHandler itemTableHandler = new InstallationItemTableHandler(context);
        String username = new SharedPreference(context).getSharedPreferences().getString("USERNAME","");
        boolean result = itemTableHandler.updatePictureItem(workId, username , timestamp, type, output.getAbsolutePath()
                , location.getLongitude(), location.getLatitude(), type, DBStatus.CREATED.value);
        return result;
    }

}
