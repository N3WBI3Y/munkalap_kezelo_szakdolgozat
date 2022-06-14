package com.giganet.giganet_worksheet.Persistence.Worksheet.Entities;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

    public class InstallationTaskEntity {
    @SerializedName("id")
    private final int id;

    @SerializedName("version")
    private final int version;

    @SerializedName("serviceId")
    private final String serviceId;

    @SerializedName("clientId")
    private final String clientId;

    @SerializedName("clientName")
    private final String clientName;

    @SerializedName("city")
    private final String city;

    @SerializedName("address")
    private final String address;

    @SerializedName("phone")
    private final String phone;

    @SerializedName("location")
    private final GPSLocation location;

    @SerializedName("partialNumber")
    private final String partialNumber;

    @SerializedName("currentStatus")
    private final InstallationTaskStatus status;

    @SerializedName("serviceType")
    private final ServiceType serviceType;

    private final int recentlyAdded;
    private long elapsedTime;
    private int favorite;
    private final String originalSubject;

    public InstallationTaskEntity(int id, int version, String serviceId, String clientId, String clientName
            , String city, String address, String phone, GPSLocation location, String partialNumber
            , InstallationTaskStatus status, long elapsedTime, int favorite, int recentlyAdded, ServiceType serviceType, String originalSubject) {
        this.id = id;
        this.version = version;
        this.serviceId = serviceId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.location = location;
        this.partialNumber = partialNumber;
        this.status = status;
        this.elapsedTime = elapsedTime;
        this.favorite = favorite;
        this.recentlyAdded = recentlyAdded;
        this.serviceType = serviceType;
        this.originalSubject = originalSubject;
    }

    public int getId() {
        return id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public GPSLocation getLocation() {
        return location;
    }

    public String getPartialNumber() {
        return partialNumber;
    }

    public int getVersion() {
        return version;
    }

    public InstallationTaskStatus getStatus() {
        return status;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getRecentlyAdded() {
        return recentlyAdded;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getOriginalSubject() {return originalSubject; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstallationTaskEntity that = (InstallationTaskEntity) o;
        return id == that.id && status.owner.equals(that.status.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status.owner);
    }

    public static class ServiceType {
        @SerializedName("name")
        private final String name;

        public ServiceType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class GPSLocation {
        @SerializedName("longitude")
        private final float longitude;

        @SerializedName("latitude")
        private final float latitude;

        public GPSLocation(float longitude, float latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public float getLongitude() {
            return longitude;
        }

        public float getLatitude() {
            return latitude;
        }
    }

    public static class InstallationTaskStatus {
        @SerializedName("id")
        private final int id;

        @SerializedName("status")
        private String status;

        @SerializedName("subject")
        private final String subject;

        @SerializedName("userTime")
        private final String userTime;

        @SerializedName("createdTime")
        private final String createdTime;

        @SerializedName("owner")
        private final String owner;

        @SerializedName("createdBy")
        private final String createdBy;

        public InstallationTaskStatus(int id, String status, String subject, String userTime, String createdTime, String owner, String createdBy) {
            this.id = id;
            this.status = status;
            this.subject = subject;
            this.userTime = userTime;
            this.createdTime = createdTime;
            this.owner = owner;
            this.createdBy = createdBy;
        }

        public int getId() {
            return id;
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

        public String getCreatedTime() {
            return createdTime;
        }

        public String getOwner() {
            return owner;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setStatus(String status) {this.status = status;}
    }
}


