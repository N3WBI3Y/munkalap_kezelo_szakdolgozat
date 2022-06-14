package com.giganet.giganet_worksheet.View;


import static com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorWindow;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.giganet_worksheet.BuildConfig;
import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.AuthenticationContract;
import com.giganet.giganet_worksheet.Presenter.AuthenticationPresenter;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Resources.Services.LocationService;
import com.giganet.giganet_worksheet.Utils.EncryptedSharedPreference;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;

public class AuthenticationActivity extends AppCompatActivity implements AuthenticationContract.View {

    private AuthenticationContract.Presenter presenter;
    private EditText username, password;
    private AppCompatButton login;
    private ImageButton optionsButton;
    private CheckBox rememberMe;
    private ProgressBar authenticationProgress;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setClickListeners();
        checkRememberMe();
    }

    @Override
    public void onSuccess(String msg) {
        runOnUiThread(() -> {
            authenticationProgress.setVisibility(View.GONE);
            presenter.savePreference(editor, username.getText().toString(), rememberMe.isChecked());
            Intent service = new Intent(this, LocationService.class);
            service.setAction(ServiceConstants.LocationService.ACTION_START_LOCATION_SERVICE);
            startService(service);
            Intent intent = new Intent(this, WorkFlowNavigationActivity.class);
            intent.putExtra("WelcomeMsg", msg);
            startActivity(intent);
        });
    }


    @Override
    public void onError(String msg) {
        runOnUiThread(() -> {
            authenticationProgress.setVisibility(View.GONE);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void login() {
        authenticationProgress.setVisibility(View.VISIBLE);
        presenter.validate(username.getText().toString(), password.getText().toString(), this);
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ServiceConstants.LocationService.LOCATION_SERVICE_ID) {
            int result = 0;
            for (int i : grantResults) {
                result += i;
            }
            if (result == 0) {
                login.performClick();
            }
        }
    }
    
    private void init() {
        setContentView(R.layout.activity_authentication);
        presenter = new AuthenticationPresenter(this);
        username = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        login = findViewById(R.id.b_login);
        rememberMe = findViewById(R.id.cb_remember_me);
        optionsButton = findViewById(R.id.ib_options_button);
        authenticationProgress = findViewById(R.id.pb_authentication_loading);
        sharedPreferences = getSharedPreferences(SHAREDPREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setDefaultSharedPreference();
    }

    private void setDefaultSharedPreference() {
        if (sharedPreferences.getInt(ServiceConstants.Options.ALARMTIME, 0) == 0) {
            editor.putInt(ServiceConstants.Options.ALARMTIME, ServiceConstants.Options.DEFAULT_ALARM_TIME);
        }
        if (sharedPreferences.getString(ServiceConstants.Options.BACKEND, "").length() == 0) {
            editor.putString(ServiceConstants.Options.BACKEND, ServiceConstants.Options.DEFAULT_BACKEND);
        }
        if (sharedPreferences.getString(ServiceConstants.Options.AUTHENTICATION, "").length() == 0) {
            editor.putString(ServiceConstants.Options.AUTHENTICATION, ServiceConstants.Options.DEFAULT_AUTHENTICATION);
        }
        if (sharedPreferences.getString(ServiceConstants.Options.DOCUMENTATION, "").length() == 0) {
            editor.putString(ServiceConstants.Options.DOCUMENTATION, ServiceConstants.Options.DEFAULT_DOCUMENTATION);
        }
    }

    private void setClickListeners() {
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OptionDialog(getActivity());
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void checkRememberMe() {
        boolean isRememberMeChecked = sharedPreferences.getBoolean("REMEMBERME", false);
        if (isRememberMeChecked) {
            SharedPreferences encryptedSharedPreference = new EncryptedSharedPreference(this).getEncryptedSharedPreference();
            username.setText(encryptedSharedPreference.getString("EncryptedUsername", ""));
            rememberMe.setChecked(true);
            password.setText(encryptedSharedPreference.getString("EncryptedPassword", ""));
            login.performClick();
        }
        removePreviousSession();
    }

    private void removePreviousSession() {
        editor.remove("userName");
        editor.remove("token");
        editor.apply();
    }
}