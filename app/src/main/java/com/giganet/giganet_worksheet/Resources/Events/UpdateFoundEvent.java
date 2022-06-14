package com.giganet.giganet_worksheet.Resources.Events;

import com.google.android.play.core.appupdate.AppUpdateInfo;

public class UpdateFoundEvent {
    private final boolean isFound;
    private final AppUpdateInfo request;


    public UpdateFoundEvent(AppUpdateInfo request) {
        isFound = false;
        this.request = request;
    }


    public UpdateFoundEvent(boolean isFound) {
        this.isFound = isFound;
        this.request = null;
    }

    public AppUpdateInfo getRequest() {
        return request;
    }

    public boolean isFound() {
        return isFound;
    }
}
