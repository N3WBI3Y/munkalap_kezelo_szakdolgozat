package com.giganet.giganet_worksheet.Network.Worksheet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceTypeDto {

    @SerializedName("name")
    private final String name;
    @SerializedName("actions")
    private final List<Action> actionList;

    public ServiceTypeDto(String name, List<Action> actionList) {
        this.name = name;
        this.actionList = actionList;
    }

    public String getName() {
        return name;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public static class Action {
        @SerializedName("action")
        private final String action;
        @SerializedName("description")
        private final String description;
        @SerializedName("type")
        private final String type;
        @SerializedName("required")
        private final boolean required;
        @SerializedName("range")
        private final Range range;

        public Action(String action, String description, String type, boolean required, Range range) {
            this.action = action;
            this.description = description;
            this.type = type;
            this.required = required;
            this.range = range;
        }

        public String getAction() {
            return action;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }

        public Range getRange() {
            return range;
        }

        public static class Range {
            @SerializedName("min")
            private final int min;
            @SerializedName("max")
            private final int max;

            public Range(int min, int max) {
                this.min = min;
                this.max = max;
            }

            public int getMin() {
                return min;
            }

            public int getMax() {
                return max;
            }
        }
    }

}
