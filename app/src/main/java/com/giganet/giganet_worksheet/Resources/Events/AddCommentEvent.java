package com.giganet.giganet_worksheet.Resources.Events;

public class AddCommentEvent {
    private final String path;

    public AddCommentEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
