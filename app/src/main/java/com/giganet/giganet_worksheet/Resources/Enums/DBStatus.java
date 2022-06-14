package com.giganet.giganet_worksheet.Resources.Enums;

public enum DBStatus {
    CREATED("CREATED"),
    UPLOAD("UPLOAD"),
    DONE("DONE");

    public final String value;

    DBStatus(String value) {
        this.value = value;
    }
}
