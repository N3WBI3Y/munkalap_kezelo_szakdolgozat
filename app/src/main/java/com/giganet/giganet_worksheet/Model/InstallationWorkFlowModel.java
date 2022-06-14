package com.giganet.giganet_worksheet.Model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.InstallationWorkFlowContract;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationItemEntity;
import com.giganet.giganet_worksheet.Network.Worksheet.ServiceTypeDto;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationServiceTypesTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTransactionTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Events.LoadingEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Resources.Services.TimerService;
import com.giganet.giganet_worksheet.Utils.ProgressHelper;
import com.giganet.giganet_worksheet.Utils.SaveEntryToDatabase;
import com.giganet.giganet_worksheet.View.AuthenticationActivity;
import com.giganet.giganet_worksheet.View.WorkStateFragments.DrawWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.ItemWorkStateContract;
import com.giganet.giganet_worksheet.View.WorkStateFragments.ItemWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.MapWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.MultilineTextWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.PhotoWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.SingleLineTextWorkStateFragment;
import com.giganet.giganet_worksheet.View.WorkStateFragments.TextWorkStateContract;
import com.giganet.giganet_worksheet.View.WorkStateFragments.WorkStateFragmentInterface;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstallationWorkFlowModel implements InstallationWorkFlowContract.Worksheet {
    private final int workId;
    private final String serviceId;
    private final ArrayList<WorkStateFragmentInterface> workStates;
    private final String username;
    private final String serviceType;
    private InstallationStatusType currentStatus;

    public InstallationWorkFlowModel(int workId, InstallationStatusType currentStatus, String serviceId, String username, String serviceType) {
        this.workId = workId;
        this.currentStatus = currentStatus;
        this.serviceId = serviceId;
        this.username = username;
        this.workStates = new ArrayList<>();
        this.serviceType = serviceType;
    }

    public ArrayList<WorkStateFragmentInterface> initFragments(Context context) {
        InstallationServiceTypesTableHandler serviceTypesTableHandler = new InstallationServiceTypesTableHandler(context);
        List<ServiceTypeDto.Action> serviceRecipe = serviceTypesTableHandler.getActions(serviceType);
        if (serviceRecipe.size() == 0) {
            Intent intent = new Intent(context, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        workStates.clear();
        for (ServiceTypeDto.Action action : serviceRecipe) {
            WorkStateFragmentInterface fragment = createFragment(action);
            if (fragment != null) {
                workStates.add(createFragment(action));
            }
        }
        setEnabled();
        return workStates;
    }

    public String onFinishString() {
        StringBuilder finishedString = new StringBuilder("<p style=\"color:red\">pirossal jelölt feladatokat kötelező elvégezni\n\n</p>");
        for (WorkStateFragmentInterface workState : workStates) {
            finishedString.append(workState.onFinishString());
        }
        return finishedString.toString();
    }

    public boolean isSubmitable() {
        boolean submitable = true;
        for (WorkStateFragmentInterface workstate : workStates) {
            if (workstate.isMust()) {
                submitable &= workstate.isSet();
            }
        }
        return submitable;
    }

    @Override
    public int getWorkId() {
        return workId;
    }

    @Override
    public String getCurrentStatus() {
        return currentStatus.name();
    }

    @Override
    public void changeStatus(InstallationStatusType nextStatus, Activity context) {
        changeStatus(nextStatus, null, context);
    }

    public boolean getEnabled() {
        return Arrays.toString(InstallationStatusType.DB_USER_PAUSE_TYPES).contains(currentStatus.name());
    }

    @Override
    public void changeStatus(InstallationStatusType nextStatus, String comment, Activity context) {
        if (currentStatus.validate(nextStatus)) {
            if (Arrays.toString(InstallationStatusType.DB_USER_PAUSE_TYPES).contains(nextStatus.name())) {
                ProgressHelper.stopInProgressTasks(context);
                String information = serviceId + " " + "Szolg id folyamatban van";
                TimerService.getInstance().setTaskService(username, workId, information,context);
            } else {
                TimerService.getInstance().stopTimer(context);
            }
            SaveEntryToDatabase.saveCurrentEntry(username, workId, nextStatus.name(), comment, context);
            currentStatus = nextStatus;
            setEnabled();
        }
    }

    @Override
    public void cancelWork(InstallationStatusType nextStatus, String comment, Activity context) {
        EventBus.getDefault().post(new LoadingEvent(true));
        changeStatus(InstallationStatusType.CANCELLED, comment, context);
        onFinish(context);
        EventBus.getDefault().post(new LoadingEvent(false));
    }

    @Override
    public void submitWork(InstallationStatusType nextStatus, Activity context) {
        EventBus.getDefault().post(new LoadingEvent(true));
        changeStatus(InstallationStatusType.END, getEndWorkComment(), context);
        onFinish(context);
        EventBus.getDefault().post(new LoadingEvent(false));
    }


    private WorkStateFragmentInterface createFragment(ServiceTypeDto.Action action) {
        int maximum = action.getRange().getMax() < 0 ? 1000 : action.getRange().getMax();
        switch (action.getType()) {
            case ServiceConstants.ServiceTypes.DRAW:
                return DrawWorkStateFragment.newInstance(workId, action.getAction(), action.getType(),
                        R.drawable.signature_icon, action.isRequired());

            case ServiceConstants.ServiceTypes.MAP:
                return MapWorkStateFragment.newInstance(workId, action.getType(), action.isRequired(), action.getAction());

            case ServiceConstants.ServiceTypes.MULTILINETEXT:
                return MultilineTextWorkStateFragment.newInstance(workId, action.getType(), action.isRequired(),
                        action.getRange().getMin(), maximum, "Leírás helye", action.getAction());

            case ServiceConstants.ServiceTypes.TEXT:
                return SingleLineTextWorkStateFragment.newInstance(workId, action.getAction(), action.getType()
                        , action.isRequired(), action.getRange().getMin(), maximum, "Ide írj!");

            case ServiceConstants.ServiceTypes.NUMBER:
                return SingleLineTextWorkStateFragment.newInstance(workId, action.getAction(), action.getType()
                        , action.isRequired(), action.getRange().getMin(), maximum, "Pl.: 056");

            case ServiceConstants.ServiceTypes.PHOTO:
                return PhotoWorkStateFragment.newInstance(workId, action.isRequired()
                        , action.getType(), action.getRange().getMin(), maximum
                        , action.getAction().equals("Munkalap") || action.getAction().equals("Szerződés"), serviceId
                        , action.getAction());

            case ServiceConstants.ServiceTypes.SCAN:
                return ItemWorkStateFragment.newInstance(workId, action.getAction(), action.getType()
                        , action.isRequired(), action.getRange().getMin(), maximum,serviceType);

            default:
                return null;
        }
    }

    private void onFinish(Context context) {
        InstallationItemTableHandler tableHandler = new InstallationItemTableHandler(context);
        ArrayList<InstallationItemEntity> list = tableHandler.getWorkItems(workId, username);
        for (InstallationItemEntity item : list) {
            if (item.getStatus().equals(DBStatus.CREATED.value)) {
                tableHandler.updateStatus(item.getId(), DBStatus.UPLOAD, username);
            }
        }

        InstallationTransactionTableHandler transactionTableHandler = new InstallationTransactionTableHandler(context);
        ArrayList<InstallationTransactionEntity> transactions = transactionTableHandler.getWorkTransaction(workId,username);
        for (InstallationTransactionEntity entity : transactions){
            transactionTableHandler.updateStatus(entity.getRowId(),DBStatus.UPLOAD.value);
        }
    }

    private String getEndWorkComment() {
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;
        for (WorkStateFragmentInterface workState : workStates) {
            if (workState instanceof TextWorkStateContract.View) {
                if (counter != 0) {
                    stringBuilder.append(" | ");
                }
                stringBuilder.append(((TextWorkStateContract.View) workState).getFragmentContent());
                counter++;
            }
        }
        return stringBuilder.toString();
    }

    private void setEnabled() {
        if (Arrays.toString(InstallationStatusType.DB_USER_PAUSE_TYPES).contains(currentStatus.name())) {
            for (WorkStateFragmentInterface workState : workStates) {
                workState.setEnabled(true);
            }
        } else {
            for (WorkStateFragmentInterface workState : workStates) {
                workState.setEnabled(false);
            }
        }
    }
}
