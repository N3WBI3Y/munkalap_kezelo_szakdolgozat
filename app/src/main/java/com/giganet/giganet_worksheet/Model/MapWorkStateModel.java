package com.giganet.giganet_worksheet.Model;

import static com.giganet.giganet_worksheet.Resources.Enums.WorkState.GPS_LOCATION;

import android.content.Context;

import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.View.WorkStateFragments.MapWorkStateContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MapWorkStateModel extends WorkStateModel implements MapWorkStateContract.Model {
    private double lon;
    private double lat;
    private boolean set;

    public MapWorkStateModel(int workId, boolean must, String type, String username) {
        super(must,workId,type,username);
        set = false;

    }

    @Override
    public boolean isMust() {
        return must;
    }

    public boolean isSet() {
        return set;
    }


    @Override
    public void setCoordinates(Context context, double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
        updateLocation(lon, lat, context);
        set = true;
    }

    @Override
    public double getLon() {
        return lon;
    }

    @Override
    public double getLat() {
        return lat;
    }

    public int getWorkId() {
        return workId;
    }

    private void updateLocation(double longitude, double latitude, Context context) {
        InstallationItemTableHandler db = new InstallationItemTableHandler(context);
        db.updateLocation(workId, username, new SimpleDateFormat(ServiceConstants.DateFormat.DATE_FORMAT).format(new Date(System.currentTimeMillis() - 60 * 1000)),
                title, longitude, latitude, GPS_LOCATION, DBStatus.CREATED.value);
    }
}
