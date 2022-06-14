package com.giganet.giganet_worksheet.Resources.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;

import java.util.Timer;

public class TimerService{
    private static TimerService _instance;
    private TimerTaskService taskService;
    private Timer timer;
    private boolean isRunning = false;

    public TimerService() {

    }

    public synchronized static TimerService getInstance() {
        if (_instance == null) {
            _instance = new TimerService();
        }
        return _instance;
    }

    public void stopTimer(Context context) {
        if (isRunning) {
            timer.cancel();
            taskService = null;
            isRunning = false;
            SharedPreferences.Editor editor = context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).edit();
            editor.putBoolean("NEEDUPDATE", false);
            editor.apply();
            stopAlarm(context);
        }
    }

    public TimerService setTaskService(String username, int workId, String information, Context context) {
        if (taskService == null || taskService.getWorkId() != workId) {
            stopTimer(context);
            taskService = new TimerTaskService(username, workId, context);
            startAlarm(username, workId, information,context);
            startTimer(workId,context);
        }
        return _instance;
    }

    private void startTimer(int workId,Context context) {
        timer = new Timer();
        timer.schedule(taskService, 60000, 60000);
        isRunning = true;
        SharedPreferences.Editor editor = context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putLong("LASTUPDATE", System.currentTimeMillis());
        editor.putInt("WORKID", workId);
        editor.apply();
    }

    private void startAlarm(String name, int id, String information,Context context) {
        int alarmTime = context.getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).getInt("ALARMTIMER", 0);
        InstallationTaskTableHandler db = new InstallationTaskTableHandler(context);
        long elapsedTime = db.getElapsedTime(name, id);
        if (alarmTime > elapsedTime) {
            Intent alarmIntent = new Intent(context, OnGoingAlarmBroadcastReceiver.class);
            alarmIntent.putExtra("ExtraInfo", information);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            alarmManager.set(AlarmManager.RTC_WAKEUP
                    , System.currentTimeMillis() + (alarmTime - elapsedTime) * 60000, pendingIntent);
        }
    }

    private void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, OnGoingAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, 0);

        alarmManager.cancel(pendingIntent);
    }
}
