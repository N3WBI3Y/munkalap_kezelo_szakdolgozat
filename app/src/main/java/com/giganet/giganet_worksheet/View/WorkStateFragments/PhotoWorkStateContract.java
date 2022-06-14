package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.content.ClipData;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.giganet.giganet_worksheet.Resources.Events.NumberOfPictureChangeEvent;
import com.giganet.giganet_worksheet.Resources.Events.RemovePictureEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public interface PhotoWorkStateContract {
    interface Model {
        boolean isMust();

        ArrayList<String> getPictures();

        void copySelectedFiles(Uri image, Location location, Context context);

        void copySelectedFiles(ClipData data, Location location, Context context);

        boolean isSet();

        String getType();

        String getPhotoPath();

        int getWorkId();

        int getMaxNumberOfPicture();

        boolean isNeedSerialNumberScan();

        boolean isSerialNumberScanned();

        void setSerialNumberScanned(boolean scanned);

        String getServiceId();
    }

    interface Presenter {
        boolean isMust();

        void copySelectedFiles(Uri image, Location location, Context context);

        void copySelectedFiles(ClipData data, Location location, Context context);

        boolean isSet();

        void updateAdapter();

        void registForEvents();

        void unregistForEvents();

        boolean isNeedSerialNumberScan();

        boolean isSerialNumberScanned();

        void setSerialNumberScanned(boolean scanned);

        String getServiceId();

        String getPhotoPath();

        int getId();

        int getMaxNumberOfPicture();

        String getType();

        @Subscribe
        void onNumberOfPictureChangeEvent(NumberOfPictureChangeEvent event);

        @Subscribe
        void onRemovePictureEvent(RemovePictureEvent event);

    }

    interface View {
        void setAdapter(ArrayList<String> pictures);
    }
}
