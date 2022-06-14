package com.giganet.giganet_worksheet;

import android.app.Activity;
import android.content.Context;

import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Events.CancelWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.LoadingEvent;
import com.giganet.giganet_worksheet.Resources.Events.PauseWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.RestartWorkEvent;
import com.giganet.giganet_worksheet.Resources.Events.SubmitWorkEvent;
import com.giganet.giganet_worksheet.View.WorkStateFragments.WorkStateFragmentInterface;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public interface InstallationWorkFlowContract {

    interface Worksheet {
        ArrayList<WorkStateFragmentInterface> initFragments(Context context);

        int getWorkId();

        String getCurrentStatus();

        void changeStatus(InstallationStatusType nextStatus, Activity context);

        void changeStatus(InstallationStatusType nextStatus, String comment, Activity context);

        void cancelWork(InstallationStatusType nextStatus, String comment, Activity context);

        void submitWork(InstallationStatusType nextStatus, Activity context);

        String onFinishString();

        boolean isSubmitable();

        boolean getEnabled();

    }

    interface Presenter {
        void changeStatus(InstallationStatusType nextStatus);

        String getCurrentStatus();

        int getWorkId();

        void initFragments();

        void registForEvents();

        void unregistForEvents();

        boolean getSubmitable();

        String getFinishedString();

        boolean getEnabled();

        @Subscribe
        void onRestartEvent(RestartWorkEvent event);

        @Subscribe
        void onPauseEvent(PauseWorkEvent event);

        @Subscribe
        void onCancelWorkEvent(CancelWorkEvent event);

        @Subscribe
        void onSubmitWorkEvent(SubmitWorkEvent event);


    }

    interface View {
        Activity getView();

        void initFragments(ArrayList<WorkStateFragmentInterface> fragmentInterfaces);

        void onStatusChange();

        void onFinish();

        @Subscribe
        void onLoadingEvent(LoadingEvent event);

    }
}
