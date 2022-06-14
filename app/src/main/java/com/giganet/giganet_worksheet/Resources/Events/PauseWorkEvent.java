package com.giganet.giganet_worksheet.Resources.Events;

public class PauseWorkEvent {
    private final String comment;

    public PauseWorkEvent(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
