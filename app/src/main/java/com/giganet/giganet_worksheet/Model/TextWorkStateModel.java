package com.giganet.giganet_worksheet.Model;

import android.content.Context;
import android.location.Location;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Resources.Enums.WorkState;
import com.giganet.giganet_worksheet.View.WorkStateFragments.TextWorkStateContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TextWorkStateModel extends WorkStateModel implements TextWorkStateContract.Model {
    private final int minLength;
    private final int maxLength;
    private String text;

    public TextWorkStateModel(String username, boolean must, int workId, String type, int minLength, int maxLength, String title) {
        super(must,workId,title,username);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean isSet() {
        return text.length() >= minLength;
    }

    @Override
    public boolean isMust() {
        return must;
    }

    @Override
    public void saveText(Context context, String text, Location location) {
        try {
            this.text = text;
            InstallationItemTableHandler db = new InstallationItemTableHandler(context);
            if (text.length() > 0) {
                if (location != null) {
                    db.updateTextItem(workId, username, new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), title
                            , location.getLongitude(), location.getLatitude(), text.replace('"', '`'), DBStatus.CREATED.value);
                } else {
                    db.updateTextItem(workId, username,new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)), WorkState.WORKSHEET_COMMENT.toString()
                            , 0.0, 0.0, text.replace('"', '`'), DBStatus.CREATED.value);
                }
            } else {
                db.deleteTextItem(workId, title, username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String loadText(Context context) {
        InstallationItemTableHandler db = new InstallationItemTableHandler(context);
        text = db.getTextItem(workId, title, username);
        return text;
    }

    @Override
    public String getText() {
        return title + ": " + text;
    }
}
