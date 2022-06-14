package com.giganet.giganet_worksheet.Network.Worksheet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MaterialDto {

    @SerializedName("name")
    private final String name;
    @SerializedName("materials")
    private final List<Material> materialList;

    public MaterialDto(String name, List<Material> materialList) {
        this.name = name;
        this.materialList = materialList;
    }

    public String getName() {
        return name;
    }

    public List<Material> getMaterialList() {
        return materialList;
    }

    public static class Material{
        @SerializedName("material")
        private final String material;
        @SerializedName("unit")
        private final String unit;
        @SerializedName("serialNumber")
        private final boolean serialNumber;

        public Material(String material, String unit, boolean serialNumber) {
            this.material = material;
            this.unit = unit;
            this.serialNumber = serialNumber;
        }

        public String getMaterial() {
            return material;
        }

        public String getUnit() {
            return unit;
        }

        public boolean isSerialNumber() {
            return serialNumber;
        }
    }
}