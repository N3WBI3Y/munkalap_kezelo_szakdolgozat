package com.giganet.giganet_worksheet.Resources.Constants;

import android.graphics.Color;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ServiceConstants {
    public static class LocationService {
        public static final int LOCATION_SERVICE_ID = 175;
        public static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
        public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
        public static final long TIME_BETWEEN_TWO_UPDATE = 1000 * 60;
        public static final long FASTEST_TIME_BETWEEN_UPDATE = 1000 * 15;
    }

    public static class CameraService {
        public static final int CAMERA_SERVICE_ID = 100;
        public static final int REQUEST_PICTURE_CAPTURED = 101;
        public static final int DEFAULT_WIDTH = 2592;
        public static final int DEFAULT_HEIGHT = 1944;
    }

    public static class Documentation {
        public static final String DOCUMENTATIONNOTIFYCHANNELNAME = "DOCUMENTATIONNOTIFYCHANNELID";
        public static final String NOTIFICATIONNAME = "DokumentációMentése";
        public static final int NOTIFICATIONID = 105;
        public static final int FINISHEDNOTIFICATIONID = 106;

    }

    public static class WorksheetInstallation {
        public static final String WORKSHEETNOTIFYCHANNELNAME = "WORKSHEETNOTIFYCHANNELID";
        public static final String WORKSHEETNOTIFICATIONNAME = "KépekFeltöltése";
        public static final int WORKSHEETNOTIFICATIONID = 107;

        public static final String WORKSHEETSYCNCHANNELNAME = "WORKSHEETSYCNCHANNELNAMENOTIFYCHANNELID";
        public static final String WORKSHEETSYCNCNOTIFICATIONNAME = "SZINKRONIZÁLÁSÁLLAPOTA";
        public static final int WORKSHEETSYCNCNOTIFICATIONID = 108;

    }

    public static class AlarmNotification {
        public static final String ALARMNOTIFICATIONCHANNELNAME = "ALARMNOTIFICATIONCHANNELNAME";
        public static final int ALARMNOTIFICATIONCHANNELID = 102;
    }

    public static class NewTaskNotificaiton {
        public static final String NEWTASKNOTIFICATIONCHANNELNAME = "NEWTASKNOTIFICATIONCHANNELNAME";
        public static final int NEWTASKNOTIFICATIONCHANNELID = 103;
    }

    public static class DateFormat {
        public static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    }

    public static class VersionControl {
        public static final int DATABASEVERSION = 36;
        public static final int APP_UPDATE_ID = 62;
        public static final String CURSORWINDOWSIZE = "sCursorWindowSize";
    }

    public static class ServiceTypes {
        public static final String DRAW = "DRAW";
        public static final String SCAN = "SCAN";
        public static final String MAP = "MAP";
        public static final String MULTILINETEXT = "MULTILINETEXT";
        public static final String TEXT = "TEXT";
        public static final String NUMBER = "NUMBER";
        public static final String PHOTO = "PHOTO";
    }

    public static class Draw{
        public static final int BRUSH_SIZE = 20;
        public static final int BRUSH_COLOR = Color.BLACK;
        public static final int BG_COLOR = Color.WHITE;
        public static final float TOUCH_TOLERANCE = 4;

    }

    public static class Options{
        public static final String ALARMTIME  = "alarmtime";
        public static final String BACKEND = "backend";
        public static final String DOCUMENTATION = "documentation";
        public static final String AUTHENTICATION = "authentication";

        public static final int DEFAULT_ALARM_TIME = 90;
        public static final String DEFAULT_AUTHENTICATION = "https://sso.giganet.hu/";
        public static final String DEFAULT_DOCUMENTATION = "http://gpon.giganet.hu:3337/api/";
        public static final String DEFAULT_BACKEND = "https://worksheetjs.giganet.hu/api/";
    }
}
