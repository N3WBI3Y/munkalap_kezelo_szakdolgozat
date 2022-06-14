package com.giganet.giganet_worksheet.Presenter;

import com.giganet.giganet_worksheet.InstallationWorkFlowContract;
import com.giganet.giganet_worksheet.Model.InstallationWorkFlowModel;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Events.CancelWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.PauseWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.RestartWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.SubmitWorkEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class InstallationWorkFlowPresenter implements InstallationWorkFlowContract.Presenter {
    InstallationWorkFlowContract.View view;
    InstallationWorkFlowContract.Worksheet model;

    public InstallationWorkFlowPresenter(InstallationWorkFlowContract.View view, int workId, String currentStatus, String serviceId
            , String username, String serviceType) {
        this.view = view;
        this.model = new InstallationWorkFlowModel(workId, InstallationStatusType.stringToInstallationStatusType(currentStatus), serviceId, username, serviceType);
    }


    @Override
    public void changeStatus(InstallationStatusType nextStatus) {
        model.changeStatus(nextStatus, view.getView());
        view.onStatusChange();
    }

    @Override
    public String getCurrentStatus() {
        return model.getCurrentStatus();
    }

    @Override
    public int getWorkId() {
        return model.getWorkId();
    }

    @Override
    public void initFragments() {
        view.initFragments(model.initFragments(view.getView()));
    }

    @Override
    @Subscribe
    public void onPauseEvent(PauseWorkEvent event) {
        model.changeStatus(InstallationStatusType.PAUSED, event.getComment(), view.getView());
        view.onStatusChange();
    }

    @Override
    @Subscribe
    public void onRestartEvent(RestartWorkEvent event){
        model.changeStatus(InstallationStatusType.STARTED,event.getComment(),view.getView());
        view.onStatusChange();
    }

    @Override
    @Subscribe
    public void onCancelWorkEvent(CancelWorkEvent event) {
        model.cancelWork(InstallationStatusType.CANCELLED, event.getComment(), view.getView());
        view.onFinish();
    }

    @Override
    @Subscribe
    public void onSubmitWorkEvent(SubmitWorkEvent event) {
        model.submitWork(InstallationStatusType.END, view.getView());
        view.onFinish();
    }

    @Override
    public void registForEvents() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void unregistForEvents() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean getSubmitable() {
        return model.isSubmitable();
    }

    @Override
    public String getFinishedString() {
        return model.onFinishString();
    }

    @Override
    public boolean getEnabled() {
        return model.getEnabled();
    }
}
