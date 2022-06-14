package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Presenter.TextWorkStatePresenter;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.CheckMarkSetter;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MultilineTextWorkStateFragment extends Fragment implements TextWorkStateContract.View, View.OnFocusChangeListener
        , WorkStateFragmentInterface {

    private TextWorkStateContract.Presenter presenter;
    private boolean enabled;
    private EditText input;
    private LocationService locationService;
    private ImageView checkMark;
    private String titleString;
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


    public MultilineTextWorkStateFragment() {
    }

    public static MultilineTextWorkStateFragment newInstance(int workId, String type, boolean must, int minLength, int maxLength, String hint, String title) {
        Bundle data = new Bundle();
        data.putInt("workId", workId);
        data.putString("type", type);
        data.putBoolean("must", must);
        data.putInt("minLength", minLength);
        data.putInt("maxLength", maxLength);
        data.putString("hint", hint);
        data.putString("title", title);
        MultilineTextWorkStateFragment fragment = new MultilineTextWorkStateFragment();
        fragment.setArguments(data);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment_work_state, container, false);
        Bundle data = getArguments();
        if (data == null) {
            requireActivity().finish();
            return v;
        }
        init(v, data);

        return v;
    }

    @Override
    public boolean isMust() {
        return presenter.isMust();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (input != null) {
            input.setEnabled(enabled);
        }
    }

    @Override
    public String onFinishString() {
        if (presenter.isMust()) {
            return presenter.isSet() ? "" : "<p style=\"color:red\">Még nem adtál leírást a " + titleString + " munkafolyamathoz\n</p>";
        }
        return presenter.isSet() ? "" : "<p>Még nem adtál leírást a " + titleString + " munkafolyamathoz\n</p>";
    }


    @Override
    public boolean isSet() {
        return presenter.isSet();
    }


    @Override
    public void setText(String text) {
        input.setText(text);
    }

    @Override
    public void setCheckMark() {
        try {
            CheckMarkSetter.setCheckMark(checkMark, presenter.isSet(), presenter.isMust(), requireActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFragmentContent() {
        return presenter.getText();
    }

    private void init(View v, Bundle data) {
        checkMark = v.findViewById(R.id.iv_check_mark);
        input = v.findViewById(R.id.t_multiline_string_input);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(data.getInt("maxLength"))});
        if (data != null) {
            String username = new SharedPreference(requireActivity()).getSharedPreferences().getString("USERNAME","");
            input.setHint(data.getString("hint") + (data.getBoolean("must") ? "\n(kötelező)" : ""));
            presenter = new TextWorkStatePresenter(this, data.getBoolean("must"), data.getInt("workId")
                    , data.getString("type"), data.getInt("minLength")
                    , data.getInt("maxLength"), data.getString("title"), username);
            titleString = data.getString("title");
        }
        presenter.setText();
        input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        bindService();
        input.setOnFocusChangeListener(this);

    }

    private void bindService() {
        Intent bindIntent = new Intent(requireActivity(), LocationService.class);
        requireActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            locationService.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    task.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            presenter.saveText(input.getText().toString(), location);
                        }
                    });
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            presenter.saveText(input.getText().toString(), null);
                        }
                    });
                }
            });
        }
    }
}