package com.giganet.giganet_worksheet.Resources.Events;

public class RemovePictureEvent {
    private final String path;
    private String type;

    public RemovePictureEvent(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public RemovePictureEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }


    public String getType() {
        return type;
    }
}
