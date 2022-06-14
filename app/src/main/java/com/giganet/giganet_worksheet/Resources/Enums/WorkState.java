package com.giganet.giganet_worksheet.Resources.Enums;

public enum WorkState {
    WORKSHEET_COMMENT,
    DOCUMENTATION,
    BEGIN,
    PAUSED,
    STARTED,
    END,
    CANCELLED,
    ISSUED,
    LOCATION,
    BARCODE_SCANNED,
    BOX_NUMBER;

    public static final String[] STATUS_CHANGED = {CANCELLED.toString(), BEGIN.toString(), PAUSED.toString(), STARTED.toString(), END.toString(), ISSUED.toString()};
    public static final String GPS_LOCATION = LOCATION.toString();

    public static String GetStatusChangedString() {

        StringBuilder sr = new StringBuilder();
        sr.append(" ( ");
        for (int i = 0; i < STATUS_CHANGED.length; ++i) {
            if (i == STATUS_CHANGED.length - 1) {
                sr.append("'").append(STATUS_CHANGED[i]).append("'");
            } else {
                sr.append("'").append(STATUS_CHANGED[i]).append("'").append(", ");
            }
        }
        sr.append(" ) ");
        return sr.toString();
    }
}
