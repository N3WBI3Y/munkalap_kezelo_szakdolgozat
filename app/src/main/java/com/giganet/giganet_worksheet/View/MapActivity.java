package com.giganet.giganet_worksheet.View;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.giganet_worksheet.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap googleMap;
    private Button back, save;
    private LatLng lastMarkerPos;
    private TextView information;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_map);
        back = findViewById(R.id.b_back);
        save = findViewById(R.id.b_save);
        information = findViewById(R.id.t_INFORMATION);
        Intent data = getIntent();
        lastMarkerPos = new LatLng(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));

        back.setOnClickListener(this);
        save.setOnClickListener(this);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.addMarker(new MarkerOptions().position(lastMarkerPos));

        String coordinate = String.format("%f, %f", lastMarkerPos.longitude, lastMarkerPos.latitude);
        String address = coordinateToAddress(lastMarkerPos);

        SpannableString span1 = new SpannableString(coordinate + "\n\n" + address);
        span1.setSpan(new RelativeSizeSpan(1.8f), 0, coordinate.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        information.setText(span1, TextView.BufferType.SPANNABLE);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastMarkerPos).zoom(15).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " " + latLng.longitude);
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                googleMap.addMarker(markerOptions);
                lastMarkerPos = latLng;
                String coordinate = String.format("%f,  %f", latLng.longitude, latLng.latitude);
                String address = coordinateToAddress(lastMarkerPos);

                SpannableString span1 = new SpannableString(coordinate + "\n\n" + address);
                span1.setSpan(new RelativeSizeSpan(1.8f), 0, coordinate.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                information.setText(span1, TextView.BufferType.SPANNABLE);

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_back) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("longitude", lastMarkerPos.longitude);
            returnIntent.putExtra("latitude", lastMarkerPos.latitude);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private String coordinateToAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
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
        return address;
    }
}