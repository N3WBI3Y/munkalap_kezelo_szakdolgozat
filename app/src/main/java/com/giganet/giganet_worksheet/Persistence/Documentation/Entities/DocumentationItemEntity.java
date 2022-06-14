package com.giganet.giganet_worksheet.Persistence.Documentation.Entities;

public class DocumentationItemEntity {
    private final int documentId;
    private final String photoPath;
    private final String userId;
    private final String status;
    private final String subject;
    private final String createdTime;
    private final String externalIdentifier;

    public DocumentationItemEntity(int documentId, String photoPath, String userId, String status, String subject, String createdTime, String externalIdentifier) {
        this.documentId = documentId;
        this.photoPath = photoPath;
        this.userId = userId;
        this.status = status;
        this.subject = subject;
        this.createdTime = createdTime;
        this.externalIdentifier = externalIdentifier;
    }

    public int getDocumentId() {
        return documentId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getExternalIdentifier() {
        return externalIdentifier;
    }
}
