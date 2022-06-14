package com.giganet.giganet_worksheet.Resources.Services;


import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private BroadcastReceiver broadcastReceiver;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ServiceConstants.LocationService.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(ServiceConstants.LocationService.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    public Task<Location> getCurrentLocation() {
        return LocationServices.getFusedLocationProviderClient(this).getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        });
    }

    @SuppressLint("MissingPermission")
    public Task<Location> getLastLocation() {
        return LocationServices.getFusedLocationProviderClient(this).getLastLocation();
    }


    @SuppressLint("MissingPermission")
    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle("Running");
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);


        if (notificationManager != null
                && notificationManager.getNotificationChannel(channelId) == null) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationChannel.setDescription("This channel is used by location service");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent locationIntent = new Intent("LOCATION");

                getCurrentLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    locationIntent.putExtra("LONGITUDE", location.getLongitude());
                                    locationIntent.putExtra("LATITUDE", location.getLatitude());
                                    locationIntent.putExtra("DATE", new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)));
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(locationIntent);
                                }
                            }
                        });

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                locationIntent.putExtra("LONGITUDE", -1);
                                locationIntent.putExtra("LATITUDE", -1);
                                locationIntent.putExtra("DATE", new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)));
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(locationIntent);
                            }
                        });
                    }
                });
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter("getLocation"));
    }


    private void stopLocationService() {
        stopForeground(true);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        stopSelf();
    }


    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}