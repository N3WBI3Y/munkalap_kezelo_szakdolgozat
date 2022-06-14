package com.giganet.giganet_worksheet.Resources.Events;

public class RestartWorkEvent {
    private final String comment;

    public RestartWorkEvent(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

}
