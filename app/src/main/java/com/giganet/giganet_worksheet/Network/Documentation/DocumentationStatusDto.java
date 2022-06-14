package com.giganet.giganet_worksheet.Network.Documentation;

import com.google.gson.annotations.SerializedName;

public class DocumentationStatusDto {

    @SerializedName("id")
    private final int id;

    @SerializedName("name")
    private final String name;

    @SerializedName("priority")
    private final int priority;

    public DocumentationStatusDto(int id, String name, int priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }
}
