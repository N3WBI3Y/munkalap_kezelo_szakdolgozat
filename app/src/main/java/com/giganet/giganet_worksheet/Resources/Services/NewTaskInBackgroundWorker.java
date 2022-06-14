package com.giganet.giganet_worksheet.Resources.Services;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants.NewTaskNotificaiton.NEWTASKNOTIFICATIONCHANNELNAME;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences;
import com.giganet.giganet_worksheet.Utils.RunningProcessHelper;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.View.AuthenticationActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTaskInBackgroundWorker extends Worker {


    public NewTaskInBackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            return Result.success();
        }
        android.content.SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USERNAME", "");
        if (username == null || username.equals("")) {
            return Result.success();
        }

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 7 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 18) {
            return Result.success();
        }

        InstallationTaskTableHandler db = new InstallationTaskTableHandler(getApplicationContext());
        final ArrayList<InstallationTaskEntity> taskOnDevice = db.getAllTask(username);
        SSOService.getToken(getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                Call<List<InstallationTaskEntity>> call = RetrofitClientWorksheet.getInstance(getApplicationContext()).getApi().getAllOpenTasks(token);
                call.enqueue(new Callback<List<InstallationTaskEntity>>() {
                    @Override
                    public void onResponse(Call<List<InstallationTaskEntity>> call, Response<List<InstallationTaskEntity>> response) {
                        int numberOfNewTask = 0;
                        String taskDescription = "";
                        if (response.body() != null) {
                            for (InstallationTaskEntity task : response.body()) {
                                if (!taskOnDevice.contains(task) && !task.getStatus().getStatus().equals("")) {
                                    numberOfNewTask++;
                                    taskDescription = task.getClientName() + " \n" + task.getAddress();
                                }
                            }
                        }
                        if (numberOfNewTask == 1 && !RunningProcessHelper.isApplicationRunning(getApplicationContext(), "com.giganet.giganet_worksheet")) {
                            createNotificationChannel(getApplicationContext());
                            createNotification("Új feladatot kaptál!", taskDescription);


                        } else if (numberOfNewTask > 1 && !RunningProcessHelper.isApplicationRunning(getApplicationContext(), "com.giganet.giganet_worksheet")) {
                            createNotificationChannel(getApplicationContext());
                            createNotification("Új feladatokat kaptál!", "Több új feladatot kaptál");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<InstallationTaskEntity>> call, Throwable t) {
                    }
                });
            }

            @Override
            public void onFailure(String token) {
                Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext().getApplicationContext(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
            }
        });

        return Result.success();
    }


    private void createNotificationChannel(Context context) {
        String name = "Új feladatok csatorna";
        String description = "Új feladatot/okat kaptál";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(NEWTASKNOTIFICATIONCHANNELNAME, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NEWTASKNOTIFICATIONCHANNELNAME);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        builder.setContentText(content);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        final Intent notificationIntent = new Intent(getApplicationContext(), AuthenticationActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        notificationManagerCompat.notify(ServiceConstants.NewTaskNotificaiton.NEWTASKNOTIFICATIONCHANNELID, builder.build());
    }
}
