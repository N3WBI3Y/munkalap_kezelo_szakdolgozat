package com.giganet.giganet_worksheet.Utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.giganet.giganet_worksheet.Resources.Services.SaveLocationServiceWorker;

public class SaveEntryToDatabase {
    public static void saveCurrentEntry(String username, int workId, String status, String comment, Context context) {
        Data periodicData = new Data.Builder().putString("username", username).putInt("workID", workId).putString("status", status).putString("comment", comment).build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SaveLocationServiceWorker.class)
                .setConstraints(Constraints.NONE)
                .setInputData(periodicData)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(String.valueOf(workId), ExistingWorkPolicy.APPEND_OR_REPLACE, work);
    }
}
