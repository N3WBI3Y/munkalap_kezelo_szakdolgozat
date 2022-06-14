package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.Context;

import com.giganet.giganet_worksheet.Resources.Events.LocationEvent;

import org.greenrobot.eventbus.Subscribe;

public interface MapWorkStateContract {
    interface Model {
        boolean isMust();

        boolean isSet();

        void setCoordinates(Context context, double lon, double lat);

        double getLon();

        double getLat();

        String getTitle();

        String getUsername();
    }

    interface Presenter {
        boolean isMust();

        boolean isSet();

        void setCoordinates(double lon, double lat);

        void registForEvents();

        void unregistForEvents();
        
        String getTitle();
        
        String getUsername();

        @Subscribe
        void onNewLocationEvent(LocationEvent event);

    }

    interface View {
        Activity getActivity();

        void setCoordinates(double longi, double lati);
    }
}
