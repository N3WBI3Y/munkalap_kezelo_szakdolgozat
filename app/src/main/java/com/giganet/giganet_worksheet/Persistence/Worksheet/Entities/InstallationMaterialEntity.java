package com.giganet.giganet_worksheet.Persistence.Worksheet.Entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class InstallationMaterialEntity {

    @SerializedName("id")
    private final int id;
    @SerializedName("name")
    private final String name;
    @SerializedName("unit")
    private final String unit;
    @SerializedName("serialNumber")
    private final boolean serialNum;

    public InstallationMaterialEntity(int id, String name, String unit, boolean serialNum) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.serialNum = serialNum;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isSerialNum() {
        return serialNum;
    }
}
