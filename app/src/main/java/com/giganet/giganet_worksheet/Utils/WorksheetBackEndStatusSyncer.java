package com.giganet.giganet_worksheet.Utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Services.SaveStatusToBackEndWorker;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorksheetBackEndStatusSyncer {

    public static Future<?> syncWithBackEnd(String username, int workId, InstallationStatusType oldStatus, InstallationStatusType newStatus, Context context) {
        return Executors.newSingleThreadExecutor().submit(() -> {
            InstallationItemTableHandler itemDB = new InstallationItemTableHandler(context);
            ArrayList<InstallationItemEntity> items = itemDB.getUploadableStatus(workId, username);
            if (items.size() == 0) {
                InstallationTaskTableHandler taskDBHandler = new InstallationTaskTableHandler(context);
                taskDBHandler.setTaskStatus(username, workId, newStatus.toString());
                return;
            }
            uploadStatusToBackEnd(items, username, oldStatus, context);
        });

    }

    public static void syncWithBackEnd(String username, int workId, InstallationStatusType newStatus, Context context) {
        SSOService.getToken(context, new SSOResult() {
            @Override
            public void onSuccess(String token) {
                RetrofitClientWorksheet.getInstance(context).getApi().getTask(token, String.valueOf(workId)).enqueue(new Callback<InstallationTaskEntity>() {
                    @Override
                    public void onResponse(Call<InstallationTaskEntity> call, Response<InstallationTaskEntity> response) {
                        if (response.code() == 200 && response.body() != null) {
                            syncWithBackEnd(username, workId, InstallationStatusType.valueOf(response.body().getStatus().getStatus()), newStatus, context);
                        }
                    }

                    @Override
                    public void onFailure(Call<InstallationTaskEntity> call, Throwable t) {
                    }
                });
            }

            @Override
            public void onFailure(String token) {
            }
        });
    }

    private static void uploadStatusToBackEnd(ArrayList<InstallationItemEntity> items, String username, InstallationStatusType oldStatus, Context context) {
        Data inputData = new Data.Builder()
                .putString("username", username)
                .putString("workId", String.valueOf(items.get(0).getIdWork()))
                .putInt("itemId", items.get(0).getId())
                .putString("subject", items.get(0).getComment())
                .putString("date", items.get(0).getDate())
                .putString("status", items.get(0).getWorkState())
                .putBoolean("last", items.size() == 1)
                .putString("backendStatus", oldStatus.toString()).build();


        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SaveStatusToBackEndWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(inputData)
                .build();
        WorkContinuation workContinuation = WorkManager.getInstance(context).beginUniqueWork((String.valueOf(items.get(0).getIdWork())), ExistingWorkPolicy.REPLACE, work);

        for (int i = 1; i < items.size(); ++i) {
            inputData = new Data.Builder()
                    .putString("username", username)
                    .putString("workId", String.valueOf(items.get(i).getIdWork()))
                    .putInt("itemId", items.get(i).getId())
                    .putString("subject", items.get(i).getComment())
                    .putString("date", items.get(i).getDate())
                    .putString("status", items.get(i).getWorkState())
                    .putBoolean("last", i == items.size() - 1)
                    .putString("backendStatus", oldStatus.toString()).build();

            work = new OneTimeWorkRequest.Builder(SaveStatusToBackEndWorker.class)
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setInputData(inputData)
                    .build();

            workContinuation = workContinuation.then(work);
        }
        workContinuation.enqueue();

    }
}
