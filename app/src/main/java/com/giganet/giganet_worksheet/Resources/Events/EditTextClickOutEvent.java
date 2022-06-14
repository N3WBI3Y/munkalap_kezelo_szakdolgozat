package com.giganet.giganet_worksheet.Resources.Events;

public class EditTextClickOutEvent {
    private final int workId;

    public EditTextClickOutEvent(int workId) {
        this.workId = workId;
    }

    public int getWorkId() {
        return workId;
    }
}
