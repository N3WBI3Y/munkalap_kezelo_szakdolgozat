package com.giganet.giganet_worksheet.Model;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.giganet.giganet_worksheet.OnApplicationStartupContract;
import com.giganet.giganet_worksheet.Persistence.DataBaseHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationTableHandler;
import com.giganet.giganet_worksheet.Persistence.Documentation.Entities.DocumentationEntity;
import com.giganet.giganet_worksheet.Resources.Enums.WorkflowTypes;
import com.giganet.giganet_worksheet.Resources.Events.UpdateFoundEvent;
import com.giganet.giganet_worksheet.Utils.PermissionContainer;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.Executors;

public class OnApplicationStartupModel implements OnApplicationStartupContract.Model {
    private final AppUpdateManager appUpdateManager;

    public OnApplicationStartupModel(AppUpdateManager appUpdateManager) {
        this.appUpdateManager = appUpdateManager;
        appUpdateManager.registerListener(new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(@NonNull InstallState installState) {
                if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                    EventBus.getDefault().post(new UpdateFoundEvent(true));
                }
            }
        });
    }


    @Override
    public void checkForUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnCompleteListener(new OnCompleteListener<AppUpdateInfo>() {
            @Override
            public void onComplete(@NonNull Task<AppUpdateInfo> task) {
                task.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                    @Override
                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            EventBus.getDefault().post(new UpdateFoundEvent(appUpdateInfo));
                        } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            EventBus.getDefault().post(new UpdateFoundEvent(true));
                        } else {
                            EventBus.getDefault().post(new UpdateFoundEvent(false));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        EventBus.getDefault().post(new UpdateFoundEvent(false));
                    }
                });
            }
        });
    }

    @Override
    public void cleanUp(final Activity view) {
        Executors.newFixedThreadPool(3).submit(new Runnable() {
            @Override
            public void run() {
                deleteTemporaryImageFiles(view);
                deleteTemporaryDocumentationFiles(view);
                deleteCancelledDocumentationFiles(view);
            }
        });
    }

    @Override
    public void upgradeDatabase(final Activity activity) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                DataBaseHandler dbUpdate = new DataBaseHandler(activity);
                dbUpdate.createDataBase(activity);
                dbUpdate.close();
            }
        });
    }

    @Override
    public void startUpdate() {
        appUpdateManager.completeUpdate();
    }

    @Override
    public AppUpdateManager getAppUpdateManager() {
        return appUpdateManager;
    }

    private void deleteTemporaryImageFiles(final Activity view) {
        if (PermissionContainer.CameraPermission.checkPermission(view)) {
            File[] files = view.getExternalFilesDir(null).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.listFiles() != null && file.listFiles().length == 0) {
                        file.delete();
                    }
                }
            }
        }
    }

    private void deleteTemporaryDocumentationFiles(final Activity view) {
        File[] documentationFiles = view.getExternalFilesDir(WorkflowTypes.DOCUMENTATION.type).listFiles();
        if (documentationFiles != null) {
            for (File file : documentationFiles) {
                if (file.listFiles() != null && file.listFiles().length == 0) {
                    file.delete();
                }
            }
        }
    }

    private void deleteCancelledDocumentationFiles(final Activity view) {
        DocumentationTableHandler db = new DocumentationTableHandler(view);
        for (DocumentationEntity entity : db.getCreatedDocumentations()) {
            db.removeItem(entity.getId());
        }
    }
}

