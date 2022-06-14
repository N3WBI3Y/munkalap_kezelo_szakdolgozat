package com.giganet.giganet_worksheet.Model;

import android.content.Context;
import android.util.Log;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.View.WorkStateFragments.DrawWorkStateContract;

public class DrawWorkStateModel extends WorkStateModel implements DrawWorkStateContract.Model {
    private String photoPath;

    public DrawWorkStateModel(boolean must, int workId, String title, String username) {
        super(must, workId, title, username);
    }


    @Override
    public boolean isSet() {
        return photoPath.length() != 0;
    }


    @Override
    public void savePicture(String filePath) {
        this.photoPath = filePath;
    }

    @Override
    public String loadPicture(Context context) {
        InstallationItemTableHandler itemDBHandler = new InstallationItemTableHandler(context);
        photoPath = itemDBHandler.getPhotoItem(workId, title, username);
        return photoPath;
    }
}
