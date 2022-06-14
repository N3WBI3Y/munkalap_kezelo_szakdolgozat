package com.giganet.giganet_worksheet.Utils;

import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String getCurrentDate(){
        return new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000));
    }
}
