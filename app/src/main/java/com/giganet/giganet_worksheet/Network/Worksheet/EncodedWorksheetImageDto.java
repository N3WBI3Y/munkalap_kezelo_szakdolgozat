package com.giganet.giganet_worksheet.Network.Worksheet;

import com.giganet.giganet_worksheet.Network.EncodedImagesDto;
import com.google.gson.annotations.SerializedName;

import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncodedWorksheetImageDto extends EncodedImagesDto {
    @SerializedName("type")
    private int type;
    @SerializedName("fileExt")
    private String fileExt;
    @SerializedName("subject")
    private String subject;
    @SerializedName("content")
    private String content;
    @SerializedName("hash")
    private String hash;
    @SerializedName("createdTime")
    private String createdTime;


    public EncodedWorksheetImageDto(String type, String subject, String picturePath, String createdTime) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            this.type = getType(type);
            this.fileExt = "jpg";
            this.subject = subject;
            this.content = encodeFileToBase64Binary(getStreamByteFromImage(reduceSizeOfPicture(new File(picturePath))));

            byte[] hashbytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
            this.hash = new String(Hex.encode(hashbytes));
            this.createdTime = createdTime;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getType() {
        return type;
    }

    public String getFileExt() {
        return fileExt;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public int getType(String type) {
        switch (type) {
            case "Szerződés":
                return 1;
            case "Munkalap":
                return 2;
            case "Aláírás":
                return 4;
            case "Antenna":
                return 5;
            default:
                return 3;
        }
    }
}
