package com.giganet.giganet_worksheet.Resources.Events;

public class DocumentStatusUpdateEvent {
    private final int progress;
    private final String msg;

    public DocumentStatusUpdateEvent(int progress, String msg) {
        this.progress = progress;
        this.msg = msg;
    }

    public int getProgress() {
        return progress;
    }
    public String getMsg() { return msg;}
}
