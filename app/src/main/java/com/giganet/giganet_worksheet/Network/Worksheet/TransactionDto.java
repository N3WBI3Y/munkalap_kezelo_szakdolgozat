package com.giganet.giganet_worksheet.Network.Worksheet;

import com.google.gson.annotations.SerializedName;

public class TransactionDto {
    @SerializedName("material")
    private final String material;

    @SerializedName("quantity")
    private final int quantity;

    @SerializedName("serialNumber")
    private final String serialNumber;

    @SerializedName("date")
    private final String date;


    public TransactionDto(String material, int quantity, String serialNumber, String date) {
        this.material = material;
        this.quantity = quantity;
        this.serialNumber = serialNumber;
        this.date = date;
    }

    public String getMaterial() {
        return material;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDate() {
        return date;
    }
}
