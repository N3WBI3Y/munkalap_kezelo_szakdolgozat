package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTaskEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationItemTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTaskTableHandler;
import com.giganet.giganet_worksheet.Presenter.MapWokStatePresenter;
import com.giganet.giganet_worksheet.Resources.Events.LoadingEvent;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.CheckMarkSetter;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.TextDialogs;
import com.giganet.giganet_worksheet.View.MapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

public class MapWorkStateFragment extends Fragment implements MapWorkStateContract.View, View.OnClickListener
        , WorkStateFragmentInterface {
    private TextView lon;
    private TextView lat;
    private AppCompatButton mapButton;
    private ImageView mapCheckMark;
    private MapWorkStateContract.Presenter presenter;
    private LocationService locationService;
    private ActivityResultLauncher<Intent> coordinateResult;
    private Boolean enabled = true;
    private TextView title;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };



    public MapWorkStateFragment() {
    }

    public synchronized static MapWorkStateFragment newInstance(int workId, String type, boolean mustSet, String title) {
        Bundle data = new Bundle();
        data.putInt("workId", workId);
        data.putString("type", type);
        data.putString("title", title);
        data.putBoolean("mustSet", mustSet);
        MapWorkStateFragment mapWorkStateFragment = new MapWorkStateFragment();
        mapWorkStateFragment.setArguments(data);
        return mapWorkStateFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_workstate, container, false);
        init(v, getArguments());
        setOnClickListeners();
        presenter.registForEvents();


        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregistForEvents();

    }

    @Override
    public boolean isMust() {
        return presenter.isMust();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String onFinishString() {
        if (presenter.isMust()) {
            return presenter.isSet() ? "" : "<p style=\"color:red\">Még nem adtál meg koordinátákat a " + presenter.getTitle() + " munkafolyamathoz\n</p>";
        }
        return presenter.isSet() ? "" : "Még nem adtál meg koordinátákat a " + presenter.getTitle() + " munkafolyamathoz\n";
    }

    @Override
    public boolean isSet() {
        return presenter.isSet();
    }

    @Override
    public void setCoordinates(double longi, double lati) {
        lon.setText(String.format("Lon: %s", (double) Math.round(longi * 100000d) / 100000d));
        lat.setText(String.format("Lat: %s", (double) Math.round(lati * 100000d) / 100000d));
        CheckMarkSetter.setCheckMark(mapCheckMark, presenter.isSet(), presenter.isMust(), requireActivity());
    }


    private void init(View v, Bundle data) {
        lon = v.findViewById(R.id.t_lon);
        lat = v.findViewById(R.id.t_lat);
        mapButton = v.findViewById(R.id.b_map_button);
        mapCheckMark = v.findViewById(R.id.iv_map_check_mark);
        if (data != null) {
            String username = new SharedPreference(requireActivity()).getSharedPreferences().getString("USERNAME", "");
            title = v.findViewById(R.id.t_location);
            presenter = new MapWokStatePresenter(this, data.getInt("workId")
                    , data.getBoolean("mustSet"), data.getString("title"), username);
            initCoordinates();
            title.setText(presenter.getTitle());
        } else {
            requireActivity().finish();
        }
        bindService();
        registerCoordinateResult();
    }

    private void registerCoordinateResult() {
        coordinateResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data.getDoubleExtra("longitude", -1) != -1) {
                        double longitude = data.getDoubleExtra("longitude", -1);
                        double latitude = data.getDoubleExtra("latitude", -1);
                        if (longitude == -1 && latitude == -1) {
                            Toast.makeText(requireContext(), "Hiba történt a koordináták átadásakor", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        presenter.setCoordinates(longitude, latitude);
                    }
                }
            }
        });
    }

    private void setOnClickListeners() {
        mapButton.setOnClickListener(this);
    }

    private void initCoordinates() {
        InstallationItemTableHandler itemDBHandler = new InstallationItemTableHandler(requireActivity());
        InstallationTaskEntity.GPSLocation location = itemDBHandler.getLocation(getArguments().getInt("workId"), presenter.getUsername(), getArguments().getString("title"));
        if (location == null) {
            InstallationTaskTableHandler db = new InstallationTaskTableHandler(requireActivity());
            location = db.getLocation(getArguments().getInt("workId"));
        }
        if (location != null) {
            presenter.setCoordinates(location.getLongitude(), location.getLatitude());
        }
    }

    private void bindService() {
        Intent bindIntent = new Intent(requireActivity(), LocationService.class);
        requireActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        if (enabled) {
            if (v.getId() == R.id.b_map_button) {
                onMapClick();
            }
        } else {
            Toast.makeText(requireActivity(), "Indítsd el a munkafolyamatot", Toast.LENGTH_SHORT).show();
        }
    }

    private void onMapClick() {
        EventBus.getDefault().post(new LoadingEvent(true));
        locationService.getCurrentLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (NetworkHelper.isNetworkAvailable(requireActivity()) && location != null) {
                            Intent placePickerIntent = new Intent(requireContext(), MapActivity.class);
                            placePickerIntent.putExtra("longitude", location.getLongitude());
                            placePickerIntent.putExtra("latitude", location.getLatitude());
                            coordinateResult.launch(placePickerIntent);
                        } else {
                            TextDialogs dialog = new TextDialogs(requireActivity());
                            dialog.showLocationDialog("Nem sikerült csatlakozni a térkép szolgáltatáshoz!\nGPSben elmentett legutolsó koordináták:\nLongitude: " + location.getLongitude() + "\nLatitude: " + location.getLatitude() + "\nElfogadod a követekező koordinátákat?", location);
                        }
                        EventBus.getDefault().post(new LoadingEvent(false));
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Nem sikerült a helymeghatározás", Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new LoadingEvent(false));
                    }
                });
            }
        });
    }

}