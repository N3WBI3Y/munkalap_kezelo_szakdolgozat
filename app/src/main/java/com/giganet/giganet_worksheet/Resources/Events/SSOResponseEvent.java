package com.giganet.giganet_worksheet.Resources.Events;

public class SSOResponseEvent {
    private final boolean success;

    public SSOResponseEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
