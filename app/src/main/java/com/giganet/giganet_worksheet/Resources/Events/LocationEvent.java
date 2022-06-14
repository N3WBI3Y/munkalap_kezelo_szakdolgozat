package com.giganet.giganet_worksheet.Resources.Events;

import android.location.Location;

public class LocationEvent {
    private final Location location;

    public LocationEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
