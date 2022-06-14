package com.giganet.giganet_worksheet.Resources.Events;

public class CancelWorkEvent {
    private final String comment;

    public CancelWorkEvent(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

}
