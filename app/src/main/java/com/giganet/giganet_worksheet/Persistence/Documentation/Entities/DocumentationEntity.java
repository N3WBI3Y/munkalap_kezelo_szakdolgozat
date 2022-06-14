package com.giganet.giganet_worksheet.Persistence.Documentation.Entities;

import com.google.gson.annotations.SerializedName;

public class DocumentationEntity {

    @SerializedName("username")
    private final String userId;
    private final int id;
    @SerializedName("gpsLon")
    private final Double longitude;
    @SerializedName("gpsLat")
    private final Double latitude;
    @SerializedName("time")
    private final String createdTime;
    @SerializedName("type")
    private final String type;
    @SerializedName("desc")
    private final String subject;
    private final String status;
    private int backEndId;
    @SerializedName("city")
    private String city;

    public DocumentationEntity(String userId, int id, int backEndId, Double longitude, Double latitude, String createdTime, final String type, String subject, String status, String city) {
        this.userId = userId;
        this.id = id;
        this.backEndId = backEndId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.createdTime = createdTime;
        this.type = type;
        this.subject = subject;
        this.status = status;
        this.city = city;
    }

    public String getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }

    public int getBackEndId() {
        return backEndId;
    }

    public void setBackEndId(int backEndId) {
        this.backEndId = backEndId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getStatus() {
        return status;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public static class DocumentationEntityReceived {
        @SerializedName("taskId")
        private final Integer backendId;

        public DocumentationEntityReceived(Integer backendId) {
            this.backendId = backendId;
        }

        public Integer getBackendId() {
            return backendId;
        }
    }
}
