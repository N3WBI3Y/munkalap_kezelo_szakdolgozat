package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.OnApplicationStartupContract;
import com.giganet.giganet_worksheet.Presenter.OnApplicationStartupPresenter;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;

public class OnApplicationStartupActivity extends AppCompatActivity implements OnApplicationStartupContract.View {

    ActivityResultLauncher<Intent> onStartUpDoneActivityResultLauncher;
    private OnApplicationStartupContract.Presenter presenter;
    private TextView loadingText;
    private boolean isUpdateCanceled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void showUpdateInstall() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.authentication_content), "Frissítés letöltődött", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Telepítés", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onUpdateInstallClick();
                isUpdateCanceled = true;
            }
        });
        snackbar.show();
    }


    @Override
    public void onStartupDone() {
        Intent authenticationIntent = new Intent(this, AuthenticationActivity.class);
        startActivity(authenticationIntent);
        finish();
    }



    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void updateStartupText(String msg) {
        loadingText.setText(msg);
    }

    @Override
    public void requestUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            presenter.getAppUpdateManager().startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, OnApplicationStartupActivity.this, ServiceConstants.VersionControl.APP_UPDATE_ID);
            isUpdateCanceled = true;
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.registerForEvent();
        }
        else if (isUpdateCanceled) {
            onStartupDone();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.unregisterForEvent();
        }
    }

    private void init() {
        setContentView(R.layout.activity_on_applciation_startup);
        loadingText = findViewById(R.id.tv_loadingText);
        registerOnStartUpDoneActivityResultLauncher();
        isUpdateCanceled = false;
        presenter = new OnApplicationStartupPresenter(this, AppUpdateManagerFactory.create(this));
    }

    private void registerOnStartUpDoneActivityResultLauncher(){
        onStartUpDoneActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        OnApplicationStartupActivity.this.onStartupDone();
                    }
                });
    }
}