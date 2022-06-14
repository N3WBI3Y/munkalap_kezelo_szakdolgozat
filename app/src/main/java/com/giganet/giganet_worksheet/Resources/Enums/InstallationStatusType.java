package com.giganet.giganet_worksheet.Resources.Enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum InstallationStatusType {

    CREATED,
    ISSUED,
    CANCELLED,
    BEGIN,
    PAUSED,
    STARTED,
    END,
    CLOSED;


    public static final InstallationStatusType[] DB_USER_START_TYPES = new InstallationStatusType[]{ISSUED, PAUSED};
    public static final InstallationStatusType[] DB_USER_PAUSE_TYPES = new InstallationStatusType[]{BEGIN, STARTED};

    private static Set<InstallationStatusType> toSet(InstallationStatusType... validStatuses) {
        if (null == validStatuses || validStatuses.length == 0) {

            return new HashSet<>();
        }

        return new HashSet<>(Arrays.asList(validStatuses));
    }

    private static Map<InstallationStatusType, Set<InstallationStatusType>> getValidStatuses() {
        Map<InstallationStatusType, Set<InstallationStatusType>> result = new HashMap<>();
        result.put(CREATED, toSet(ISSUED, CANCELLED, CLOSED));
        result.put(ISSUED, toSet(CANCELLED, BEGIN));
        result.put(CANCELLED, toSet(CLOSED));
        result.put(BEGIN, toSet(CANCELLED, PAUSED, END));
        result.put(PAUSED, toSet(CANCELLED, STARTED, END));
        result.put(STARTED, toSet(CANCELLED, PAUSED, END));
        result.put(END, toSet(CANCELLED, CLOSED, STARTED));
        return result;
    }

    public static InstallationStatusType stringToInstallationStatusType(String status) {
        switch (status) {
            case "CREATED":
            case "LÉTREHOZVA":
                return CREATED;
            case "ISSUED":
            case "KIADVA":
                return ISSUED;
            case "CANCELLED":
            case "MEGHIÚSULT":
                return CANCELLED;
            case "BEGIN":
            case "FOLYAMATBAN":
                return BEGIN;
            case "PAUSED":
            case "SZÜNETELTETVE":
                return PAUSED;
            case "STARTED":
                return STARTED;
            case "END":
            case "BEFEJEZVE":
                return END;
            case "CLOSED":
            case "LEZÁRVA":
                return CLOSED;
            default:
                return null;
        }
    }

    public static String statusToHungarian(String status) {
        switch (status) {
            case "CREATED":
                return "Létrehozva";
            case "ISSUED":
                return "Kiadva";
            case "CANCELLED":
                return "Meghíúsult";
            case "BEGIN":
            case "STARTED":
                return "Folyamatban";
            case "PAUSED":
                return "Szünetel";
            case "END":
                return "Befejezve";
            case "CLOSED":
                return "Lezárva";
            default:
                return "";
        }
    }

    public boolean validate(InstallationStatusType newStatus) {
        return getValidStatuses().get(this).contains(newStatus);
    }
}
