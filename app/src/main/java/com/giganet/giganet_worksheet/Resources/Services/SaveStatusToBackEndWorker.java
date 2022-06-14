package com.giganet.giganet_worksheet.Resources.Services;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Network.Worksheet.WorksheetStatusDto;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Events.StatusStatusUpdateEvent;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.AuthenticationActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaveStatusToBackEndWorker extends Worker {
    private final String workId;
    private final int itemId;
    private final String status;
    private final String subject;
    private final String date;
    private final String backendStatus;
    private final boolean last;
    private final String username;

    public SaveStatusToBackEndWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        username = getInputData().getString("username");
        workId = getInputData().getString("workId");
        itemId = getInputData().getInt("itemId", -1);
        status = getInputData().getString("status");
        subject = getInputData().getString("subject");
        date = getInputData().getString("date");
        backendStatus = getInputData().getString("backendStatus");
        last = getInputData().getBoolean("last", false);
    }

    @NonNull
    @Override
    public Result doWork() {
        final BlockingQueue<Result> resultQueue = uploadStatus(getApplicationContext());
        Result result = null;
        try {
            result = resultQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public BlockingQueue<Result> uploadStatus(Context context) {
        final BlockingQueue<Result> resultQueue = new ArrayBlockingQueue<>(1);
        EventBus.getDefault().post(new StatusStatusUpdateEvent(1));
        if (itemId == -1) {
            EventBus.getDefault().post(new StatusStatusUpdateEvent(-1));
            resultQueue.add(Result.success());
            return resultQueue;
        }

        if (!NetworkHelper.isNetworkAvailable(context.getApplicationContext())) {
            resultQueue.add(Result.success());
            return resultQueue;
        }

        SSOService.getToken(context.getApplicationContext(), new SSOResult() {
            @Override
            public void onSuccess(String token) {
                SharedPreferences sharedPreferences = new SharedPreference(getApplicationContext()).getSharedPreferences();
                String prefferedUsername = sharedPreferences.getString("prefferedUsername", null) == null ? username : sharedPreferences.getString("prefferedUsername", "");
                RetrofitClientWorksheet.getInstance(context.getApplicationContext()).getApi().updateStatus(workId, token, new WorksheetStatusDto(status, subject, date, prefferedUsername)).enqueue(new Callback<InstallationTaskEntity>() {
                    @Override
                    public void onResponse(Call<InstallationTaskEntity> call, Response<InstallationTaskEntity> response) {
                        InstallationItemTableHandler db = new InstallationItemTableHandler(context.getApplicationContext());
                        if (response.code() == 201) {
                            db.updateStatus(itemId, DBStatus.DONE, username);
                            InstallationTaskTableHandler installationTaskTableHandler = new InstallationTaskTableHandler(context.getApplicationContext());
                            installationTaskTableHandler.incrementVersion(username, Integer.parseInt(workId));
                            resultQueue.add(Result.success());
                            EventBus.getDefault().post(new StatusStatusUpdateEvent(last ? 100 : 1));
                        } else if (response.code() > 499) {
                            resultQueue.add(Result.failure());
                        } else if (response.code() >= 400) {
                            //Todo verzió kezelés
                            InstallationTaskTableHandler taskdb = new InstallationTaskTableHandler(context.getApplicationContext());
                            db.deleteItemById(itemId, username);
                            taskdb.setTaskStatus(username, Integer.parseInt(workId), backendStatus);
                            resultQueue.add(Result.success());
                            EventBus.getDefault().post(new StatusStatusUpdateEvent(last ? 100 : 1));

                        } else {
                            resultQueue.add(Result.failure());
                            EventBus.getDefault().post(new StatusStatusUpdateEvent(-1));
                        }

                    }

                    @Override
                    public void onFailure(Call<InstallationTaskEntity> call, Throwable t) {
                        resultQueue.add(Result.retry());
                        EventBus.getDefault().post(new StatusStatusUpdateEvent(-1));
                    }
                });
            }

            @Override
            public void onFailure(String token) {
                Intent intent = new Intent(context.getApplicationContext(), AuthenticationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                resultQueue.add(Result.failure());
                context.getApplicationContext().startActivity(intent);
                Toast.makeText(context.getApplicationContext(), "Nem megfelelő felhasználó név vagy jelszó", Toast.LENGTH_SHORT).show();
            }
        });
        return resultQueue;
    }
}
