package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

public interface TextWorkStateContract {
    interface Model {
        boolean isSet();

        boolean isMust();

        void saveText(Context context, String text, Location location);

        String loadText(Context context);

        String getText();
    }

    interface Presenter {
        boolean isMust();

        boolean isSet();

        void setText();

        void saveText(String text, Location location);

        String getText();
    }

    interface View {

        Activity getActivity();

        void setText(String text);

        void setCheckMark();

        String getFragmentContent();

    }
}
