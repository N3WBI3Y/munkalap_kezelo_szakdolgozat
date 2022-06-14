package com.giganet.giganet_worksheet.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.InstallationWorkFlowContract;
import com.giganet.giganet_worksheet.Presenter.InstallationWorkFlowPresenter;
import com.giganet.giganet_worksheet.Resources.Enums.InstallationStatusType;
import com.giganet.giganet_worksheet.Resources.Events.LoadingEvent;
import com.giganet.giganet_worksheet.Utils.SharedPreference;
import com.giganet.giganet_worksheet.View.WorkStateFragments.WorkStateFragmentInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;

public class InstallationWorkFlowActivity extends AppCompatActivity implements InstallationWorkFlowContract.View
                                                                                , View.OnClickListener {
    private TextView name, worksheetId, address, phone, subject, serviceId, status, timer;
    private ImageView markerIcon, phoneIcon;
    private Button submitButton, cancelButton;
    private ImageButton backButton, statusButton;
    private ProgressBar loadingBar;
    private InstallationWorkFlowContract.Presenter presenter;
    private LinearLayoutCompat fragmentList;
    private int alarmTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installation_worksheet_view);
        Bundle data = getIntent().getExtras();
        if (data != null) {
            init(data);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        presenter.unregistForEvents();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onResume();
    }

    @Override
    public void initFragments(ArrayList<WorkStateFragmentInterface> fragmentInterfaces) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (WorkStateFragmentInterface fragmentInterface : fragmentInterfaces) {
            LinearLayoutCompat fragmentLayout = new LinearLayoutCompat(this);
            fragmentLayout.setOrientation(LinearLayoutCompat.VERTICAL);
            fragmentLayout.setId(View.generateViewId());
            ft.add(fragmentLayout.getId(), (Fragment) fragmentInterface, String.valueOf(fragmentLayout.getId()));
            fragmentList.addView(fragmentLayout);
        }
        ft.commit();
    }

    @Override
    public void onStatusChange() {
        String currentStatus = presenter.getCurrentStatus();
        statusButton.setImageDrawable(getStatusImage(currentStatus));
        status.setText(InstallationStatusType.statusToHungarian(currentStatus));
        setStatusColor(currentStatus);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    @Subscribe
    public void onLoadingEvent(LoadingEvent event) {
        if (event.isLoading()) {
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            loadingBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private Drawable getStatusImage(String status) {
        if (Arrays.toString(InstallationStatusType.DB_USER_START_TYPES).contains(status)) {
            return ResourcesCompat.getDrawable(getResources(), R.drawable.play_button_white, null);
        } else if (Arrays.toString(InstallationStatusType.DB_USER_PAUSE_TYPES).contains(status)) {
            return ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null);
        } else {
            return ResourcesCompat.getDrawable(getResources(), R.drawable.restart_button_white, null);
        }
    }

    @Override
    public void onClick(View v) {
        int view_id = v.getId();
        if (view_id == R.id.b_cancel_status && presenter.getEnabled()) {
            final TextDialogs dialog = new TextDialogs(this);
            dialog.showCancelDialog();
        } else if (view_id == R.id.t_phone || view_id == R.id.iv_phone && presenter.getEnabled()) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.getText().toString()));
            startActivity(intent);
        } else if (view_id == R.id.t_address || view_id == R.id.iv_marker && presenter.getEnabled()) {
            String destination = address.getText().toString();
            destination = destination.replace(" ", "+");
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(mapIntent);
        } else if (view_id == R.id.b_submit && presenter.getEnabled()) {
            final TextDialogs dialog = new TextDialogs(this);
            dialog.showEndDialog(presenter.getFinishedString(), presenter.getSubmitable());
        } else if (view_id == R.id.b_back) {
            onBackPressed();
        } else if (view_id == R.id.ib_status) {
            String currentStatus = presenter.getCurrentStatus();
            InstallationStatusType nextStatus = nextStatus(currentStatus);
            if (nextStatus.equals(InstallationStatusType.PAUSED)) {
                TextDialogs dialog = new TextDialogs(this);
                dialog.showPauseDialog();
            }
            else if(currentStatus.equals(InstallationStatusType.END.name())){
                TextDialogs dialogs = new TextDialogs(this);
                dialogs.showRestartDialog();
            }
            else {
                presenter.changeStatus(nextStatus(currentStatus));
            }
        }
    }


    @Override
    public Activity getView() {
        return this;
    }

    private void setTimer(long elapsedTime) {
        int hour = (int) (elapsedTime / 60);
        int minutes = (int) (elapsedTime % 60);
        timer.setText(String.format((String.format("%02d:%02d", hour, minutes))));
        if (elapsedTime > alarmTime) {
            timer.setTextColor(Color.parseColor("#FF0000"));
        } else {
            timer.setTextColor(Color.parseColor("#000000"));
        }
    }

    private InstallationStatusType nextStatus(String currentStatus) {
        switch (currentStatus) {
            case "ISSUED":
                return InstallationStatusType.BEGIN;
            case "BEGIN":
            case "STARTED":
                return InstallationStatusType.PAUSED;
            case "PAUSED":
            case "END":
                return InstallationStatusType.STARTED;
            default:
                return null;
        }
    }

    private void setStatusColor(String status) {
        if (status.equals(InstallationStatusType.ISSUED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#4277cf"));

        } else if (status.equals(InstallationStatusType.BEGIN.toString())
                || status.equals(InstallationStatusType.STARTED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#e46828"));

        } else if (status.equals(InstallationStatusType.PAUSED.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#b8b84f"));

        } else if (status.equals(InstallationStatusType.END.toString())) {
            this.status.setBackgroundColor(Color.parseColor("#5ba85b"));
        }
    }


    private void init(Bundle data) {
        fragmentList = findViewById(R.id.lw_workstate_list);
        name = findViewById(R.id.t_customer_name);
        worksheetId = findViewById(R.id.t_worksheet_id);
        serviceId = findViewById(R.id.t_service_id);
        address = findViewById(R.id.t_address);
        phone = findViewById(R.id.t_phone);
        status = findViewById(R.id.t_status);
        subject = findViewById(R.id.t_subject);
        markerIcon = findViewById(R.id.iv_marker);
        phoneIcon = findViewById(R.id.iv_phone);
        timer = findViewById(R.id.t_timer);
        submitButton = findViewById(R.id.b_submit);
        cancelButton = findViewById(R.id.b_cancel_status);
        backButton = findViewById(R.id.b_back);
        statusButton = findViewById(R.id.ib_status);
        loadingBar = findViewById(R.id.pr_loading_bar);
        alarmTime = data.getInt("alarmTime");

        SharedPreferences sharedPreferences = new SharedPreference(this).getSharedPreferences();
        String username = sharedPreferences.getString("USERNAME", "");
        presenter = new InstallationWorkFlowPresenter(this, Integer.parseInt(data.getString("id"))
                , data.getString("status"), data.getString("serviceId")
                , username, data.getString("serviceType"));
        presenter.initFragments();
        presenter.registForEvents();
        setInitialText(data);
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        markerIcon.setOnClickListener(this);
        phoneIcon.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        address.setOnClickListener(this);
        phone.setOnClickListener(this);
        statusButton.setOnClickListener(this);
    }

    private void setInitialText(Bundle data) {
        name.setText(data.getString("name"));
        worksheetId.setText(data.getString("id"));
        serviceId.setText(data.getString("serviceId"));
        address.setText(String.format("%s,  %s %s", data.getString("city"), data.getString("address"), data.getString("lot_number")));
        phone.setText(String.format("%s", data.getString("phone")));
        subject.setText(String.format("%s", data.getString("subject")));
        setTimer(data.getLong("elapsedTime"));
        onStatusChange();
    }

}