package com.giganet.giganet_worksheet.Resources.Services;

import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.AlarmNotification.ALARMNOTIFICATIONCHANNELNAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.View.AuthenticationActivity;

public class OnGoingAlarmBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);
        Bundle data = intent.getExtras();
        String information = data.getString("ExtraInfo");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALARMNOTIFICATIONCHANNELNAME);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Folyamatban lévő feladat");
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        builder.setContentText(information);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        Intent notificationIntent = new Intent(context, AuthenticationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);


        notificationManagerCompat.notify(ServiceConstants.AlarmNotification.ALARMNOTIFICATIONCHANNELID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        String name = "Figyelmezető csatorna";
        String description = "Folyamatban lévő feladatról emlékeztető";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(ALARMNOTIFICATIONCHANNELNAME, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
