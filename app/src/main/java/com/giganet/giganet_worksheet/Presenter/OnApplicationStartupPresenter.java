package com.giganet.giganet_worksheet.Presenter;

import com.giganet.giganet_worksheet.Model.OnApplicationStartupModel;
import com.giganet.giganet_worksheet.OnApplicationStartupContract;
import com.giganet.giganet_worksheet.Resources.Events.UpdateFoundEvent;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.google.android.play.core.appupdate.AppUpdateManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executors;

public class OnApplicationStartupPresenter implements OnApplicationStartupContract.Presenter {
    private final OnApplicationStartupContract.Model model;
    private final OnApplicationStartupContract.View view;

    public OnApplicationStartupPresenter(OnApplicationStartupContract.View view, AppUpdateManager appUpdateManager) {
        this.view = view;
        model = new OnApplicationStartupModel(appUpdateManager);
        Executors.newSingleThreadExecutor().submit(() -> {
            startBackgroundProcesses();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            view.updateStartupText("Frissítés keresése");
            checkForUpdate();
        });

    }

    @Subscribe
    @Override
    public void onUpdateFoundEvent(UpdateFoundEvent event) {
        if (event.getRequest() != null) {
            view.requestUpdate(event.getRequest());
            return;
        }
        if (event.isFound()) {
            view.showUpdateInstall();
        } else {
            view.onStartupDone();
        }
    }

    @Override
    public void checkForUpdate() {
        if (!NetworkHelper.isNetworkAvailable(view.getActivity())) {
            view.onStartupDone();
            return;
        }
        model.checkForUpdate();
    }

    @Override
    public void startBackgroundProcesses() {
        model.cleanUp(view.getActivity());
        model.upgradeDatabase(view.getActivity());
    }

    @Override
    public void registerForEvent() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void unregisterForEvent() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onUpdateInstallClick() {
        model.startUpdate();
    }

    @Override
    public AppUpdateManager getAppUpdateManager() {
        return model.getAppUpdateManager();
    }
}
