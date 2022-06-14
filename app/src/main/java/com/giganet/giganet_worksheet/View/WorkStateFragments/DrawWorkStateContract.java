package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.Context;

public interface DrawWorkStateContract {
    interface Model {
        boolean isSet();

        boolean isMust();

        void savePicture(String filePath);

        String loadPicture(Context context);

        String getTitle();

        int getWorkId();
    }

    interface Presenter {
        boolean isMust();

        boolean isSet();

        void setPicture();

        void savePicture(String filePath);

        String getTitle();

        int getWorkId();
    }

    interface View {

        Activity getActivity();

        void setCheckMark();

        void setPicture(String photoPath);
    }
}
