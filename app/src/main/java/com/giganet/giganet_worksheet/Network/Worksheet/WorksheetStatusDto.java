package com.giganet.giganet_worksheet.Network.Worksheet;

import com.google.gson.annotations.SerializedName;

public class WorksheetStatusDto {
    @SerializedName("status")
    private final String status;
    @SerializedName("subject")
    private final String subject;
    @SerializedName("userTime")
    private final String userTime;
    @SerializedName("owner")
    private final String owner;

    public WorksheetStatusDto(String status, String subject, String userTime, String owner) {
        this.status = status;
        this.subject = subject;
        this.userTime = userTime;
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public String getUserTime() {
        return userTime;
    }

    public String getOwner() {
        return owner;
    }
}
