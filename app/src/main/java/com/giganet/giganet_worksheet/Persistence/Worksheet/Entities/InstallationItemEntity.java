package com.giganet.giganet_worksheet.Persistence.Worksheet.Entities;

public class InstallationItemEntity {
    private final int id;
    private final int idWork;
    private final String workState;
    private final String date;
    private final String photo_path;
    private final double longitude;
    private final double latitude;
    private final String comment;
    private final String status;


    public InstallationItemEntity(int id, int idWork, String date, String workState, String photo_path, Double longitude, Double latitude, String comment, String status) {
        this.id = id;
        this.idWork = idWork;
        this.workState = workState;
        this.date = date;
        this.photo_path = photo_path;
        this.longitude = longitude;
        this.latitude = latitude;
        this.comment = comment;
        this.status = status;
    }

    public String getWorkState() {
        return workState;
    }

    public int getId() {
        return id;
    }

    public int getIdWork() {
        return idWork;
    }

    public String getDate() {
        return date;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getComment() {
        return comment;
    }

    public String getStatus() {
        return status;
    }
}
