package com.giganet.giganet_worksheet.Utils;

import static android.content.Context.MODE_PRIVATE;
import static com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Resources.Services.OnGoingAlarmBroadcastReceiver;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProgressHelper {

    public static void stopInProgressTasks(Activity activity) {
        String username = activity.getSharedPreferences(SHAREDPREFERENCES, MODE_PRIVATE).getString("USERNAME", "");
        if (username != null && username.length() > 0) {
            InstallationTaskTableHandler installationTaskTableHandler = new InstallationTaskTableHandler(activity);
            ArrayList<InstallationTaskEntity> runningTasks = installationTaskTableHandler.getRunningTasks(username);
            for (InstallationTaskEntity itemEntity : runningTasks) {
                SaveEntryToDatabase.saveCurrentEntry(username, itemEntity.getId(), InstallationStatusType.PAUSED.toString(), "", activity);
            }

            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(activity, OnGoingAlarmBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    activity, 0, alarmIntent, 0);

            alarmManager.cancel(pendingIntent);


        }
    }

    public static boolean isUploadingToBackEnd(Context context) {
            ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance(context).getWorkInfosForUniqueWork("SaveToBackEndInitializer");
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING;
            }
            return running;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
