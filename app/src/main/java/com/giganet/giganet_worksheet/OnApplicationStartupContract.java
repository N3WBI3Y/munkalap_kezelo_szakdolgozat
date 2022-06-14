package com.giganet.giganet_worksheet;

import android.app.Activity;

import com.giganet.giganet_worksheet.Resources.Events.UpdateFoundEvent;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ExecutionException;

public interface OnApplicationStartupContract {
    interface Model {
        void checkForUpdate();

        void cleanUp(Activity activity);

        void upgradeDatabase(Activity activity);

        void startUpdate();

        AppUpdateManager getAppUpdateManager();

    }

    interface Presenter {
        void checkForUpdate() throws InterruptedException;

        void startBackgroundProcesses() throws ExecutionException, InterruptedException;

        void registerForEvent();

        void unregisterForEvent();

        void onUpdateInstallClick();

        AppUpdateManager getAppUpdateManager();

        @Subscribe
        void onUpdateFoundEvent(UpdateFoundEvent event);
    }

    interface View {
        void showUpdateInstall();

        void onStartupDone();

        Activity getActivity();

        void updateStartupText(String msg);

        void requestUpdate(AppUpdateInfo appUpdateInfo);
    }
}
