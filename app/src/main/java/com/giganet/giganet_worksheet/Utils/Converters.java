package com.giganet.giganet_worksheet.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Converters {

    public static String convertDoubleToGPSData(double value) {
        //https://stackoverflow.com/questions/5280479/how-to-save-gps-coordinates-in-exif-data-on-android
        String[] convertedLocation = Location.convert(value, Location.FORMAT_SECONDS).split(":");
        int degree = Math.abs(Integer.parseInt(convertedLocation[0]));
        int seconds = (int) Math.round(Double.parseDouble(convertedLocation[2].replace(",", ".")) * 10000);
        return String.format("%d/1,%s/1,%d/10000", degree, convertedLocation[1], seconds);
    }

    public static String convertTimeToGPSTime(float value) {
        //https://stackoverflow.com/questions/4879435/android-put-gpstimestamp-into-jpg-exif-tags
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) value);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return hourOfDay + "/1," + minutes + "/1," + seconds + "/1";
    }

    public static String coordinateToAddress(LatLng latLng, Context context) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        String address = null;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
            String[] addressResult = address.split(",");
            if (addressResult.length == 3) {
                address = addressResult[0] + ", " + addressResult[1];
            } else if (addressResult.length == 2) {
                address = addressResult[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address != null ? address.split(",")[0] : null;
    }
}
