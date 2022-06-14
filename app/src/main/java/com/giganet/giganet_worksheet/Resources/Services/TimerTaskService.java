package com.giganet.giganet_worksheet.Resources.Services;

import android.content.Context;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Resources.Events.TimerTickEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.TimerTask;

public class TimerTaskService extends TimerTask {
    private final String username;
    private final int workId;
    private final Context context;

    public TimerTaskService(String username, int workId, Context context) {
        this.username = username;
        this.workId = workId;
        this.context = context;
    }

    public int getWorkId() {
        return workId;
    }

    @Override
    public void run() {
        InstallationTaskTableHandler db = new InstallationTaskTableHandler(context);
        long elapsed_Time = db.getElapsedTime(username, workId);
        elapsed_Time++;
        db.updateElapsedTime(username, workId, elapsed_Time);
        android.content.SharedPreferences.Editor editor = context.getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putLong("LASTUPDATE", System.currentTimeMillis());
        editor.putInt("WORKID", workId);
        editor.apply();

        EventBus.getDefault().post(new TimerTickEvent(elapsed_Time, workId));
    }
}
