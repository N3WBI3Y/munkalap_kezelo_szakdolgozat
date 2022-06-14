package com.giganet.giganet_worksheet.Resources.Events;


public class NewStatusEvent {
    private final int workId;
    private final String status;

    public NewStatusEvent(int workId, String status) {
        this.workId = workId;
        this.status = status;
    }

    public int getWorkId() {
        return workId;
    }

    public String getStatus() {
        return status;
    }
}
