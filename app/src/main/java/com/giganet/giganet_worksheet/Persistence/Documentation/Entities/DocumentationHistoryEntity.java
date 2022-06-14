package com.giganet.giganet_worksheet.Persistence.Documentation.Entities;

import com.google.gson.annotations.SerializedName;

public class DocumentationHistoryEntity {
    private final int imageId;
    private final String timestamp;
    private final double gpsLat;
    private final double gpsLon;
    private final String city;
    @SerializedName("desc")
    private final String description;
    private final String subject;
    private final String taskType;
    private final String image;

    public DocumentationHistoryEntity(int imageId, String timestamp, double gpsLat, double gpsLon,
                                      String city, String description, String subject, String taskType, String image) {

        this.imageId = imageId;
        this.timestamp = timestamp;
        this.gpsLat = gpsLat;
        this.gpsLon = gpsLon;
        this.city = city;
        this.description = description;
        this.subject = subject;
        this.taskType = taskType;
        this.image = image;
    }

    public int getImageId() {
        return imageId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getGpsLat() {
        return gpsLat;
    }

    public double getGpsLon() {
        return gpsLon;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public String getSubject() {
        return subject;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getImage() {
        return image;
    }
}

