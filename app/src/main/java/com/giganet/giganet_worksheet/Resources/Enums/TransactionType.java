package com.giganet.giganet_worksheet.Resources.Enums;

public enum TransactionType {

    ADD("ADD"),
    MODIFY("MODIFY");

    public final String type;

    TransactionType(String type) {
        this.type = type;
    }
}
