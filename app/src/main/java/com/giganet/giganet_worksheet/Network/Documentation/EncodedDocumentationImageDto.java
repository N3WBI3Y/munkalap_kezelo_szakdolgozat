package com.giganet.giganet_worksheet.Network.Documentation;

import com.giganet.giganet_worksheet.Network.EncodedImagesDto;
import com.google.gson.annotations.SerializedName;

import java.io.File;

public class EncodedDocumentationImageDto extends EncodedImagesDto {
    @SerializedName("taskId")
    private final int taskId;
    @SerializedName("timestamp")
    private final String timestamp;
    @SerializedName("image")
    private final String image;
    @SerializedName("subject")
    private final String subject;
    @SerializedName("externalIdentifier")
    private final String externalIdentifier;


    public EncodedDocumentationImageDto(int taskId, String timestamp, String imagePath, String subject, String externalIdentifier) throws Exception {
        this.taskId = taskId;
        this.timestamp = timestamp;
        this.image = encodeFileToBase64Binary(getStreamByteFromImage(reduceSizeOfPicture(new File(imagePath))));
        this.subject = subject;
        this.externalIdentifier = externalIdentifier;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public String getSubject() {
        return subject;
    }

}

