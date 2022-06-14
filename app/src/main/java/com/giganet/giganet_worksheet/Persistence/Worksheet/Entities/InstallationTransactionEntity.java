package com.giganet.giganet_worksheet.Persistence.Worksheet.Entities;

public class InstallationTransactionEntity {
    private final int rowId;
    private final int workId;
    private final String userId;
    private final String date;
    private final String material;
    private final int quantity;
    private final String serialNum;
    private final String status;

    public InstallationTransactionEntity(int rowId, int workId, String userId, String date, String material, int quantity, String serialNum, String status) {
        this.rowId = rowId;
        this.workId = workId;
        this.userId = userId;
        this.date = date;
        this.material = material;
        this.quantity = quantity;
        this.serialNum = serialNum;
        this.status = status;
    }

    public int getRowId() {
        return rowId;
    }

    public int getWorkId() {
        return workId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getMaterial() {
        return material;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public String getStatus() {
        return status;
    }
}
