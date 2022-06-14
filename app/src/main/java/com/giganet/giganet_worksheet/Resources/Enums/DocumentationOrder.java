package com.giganet.giganet_worksheet.Resources.Enums;

public enum DocumentationOrder {

    ASCENDING("ASC"),
    DESCENDING("DESC");

    private String value;

    DocumentationOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
