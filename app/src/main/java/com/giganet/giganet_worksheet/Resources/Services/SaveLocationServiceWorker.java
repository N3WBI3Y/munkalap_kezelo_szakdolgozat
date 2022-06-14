package com.giganet.giganet_worksheet.Resources.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.Utils.WorksheetBackEndStatusSyncer;

import java.util.Arrays;

public class SaveLocationServiceWorker extends Worker {
    private final BroadcastReceiver broadcastReceiver;
    private final int workId;
    private final String status;
    private final String comment;
    private final String username;
    private InstallationItemTableHandler db;


    public SaveLocationServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workId = getInputData().getInt("workID", -1);
        status = getInputData().getString("status");
        comment = getInputData().getString("comment");
        username = getInputData().getString("username");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getDoubleExtra("LONGITUDE", -1.0) != -1.0) {
                    Double longitude = intent.getDoubleExtra("LONGITUDE", -1.0);
                    Double latitude = intent.getDoubleExtra("LATITUDE", -1.0);
                    db = new InstallationItemTableHandler(context);
                    if (status != null && Arrays.asList(WorkState.STATUS_CHANGED).contains(status)) {
                        db.addItem(workId, username, intent.getStringExtra("DATE"), status, longitude, latitude, comment, DBStatus.UPLOAD.value);
                        InstallationTaskTableHandler installDB = new InstallationTaskTableHandler(getApplicationContext());
                        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE);
                        installDB.setTaskStatus(username == null ? sharedPreferences.getString("USERNAME", "") : username, workId, status);
                        WorksheetBackEndStatusSyncer.syncWithBackEnd(username == null ? sharedPreferences.getString("USERNAME", "") : username, workId, InstallationStatusType.stringToInstallationStatusType(status), getApplicationContext());
                    } else {
                        db.addItem(workId, username, intent.getStringExtra("DATE"), status, longitude, latitude, comment, DBStatus.CREATED.value);
                    }

                }
                LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);


            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("LOCATION"));
    }

    @NonNull
    @Override
    public Result doWork() {
        getLocation();

        return Result.success();
    }

    private void getLocation() {
        Intent intent = new Intent("getLocation");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}
