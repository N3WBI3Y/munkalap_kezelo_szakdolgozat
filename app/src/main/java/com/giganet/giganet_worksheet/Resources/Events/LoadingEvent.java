package com.giganet.giganet_worksheet.Resources.Events;

public class LoadingEvent {
    private final boolean isLoading;

    public LoadingEvent(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
