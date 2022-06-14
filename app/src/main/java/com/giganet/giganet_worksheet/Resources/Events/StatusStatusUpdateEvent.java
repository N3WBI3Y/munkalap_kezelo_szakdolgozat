package com.giganet.giganet_worksheet.Resources.Events;

public class StatusStatusUpdateEvent {
    private final int progress;

    public StatusStatusUpdateEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }
}
