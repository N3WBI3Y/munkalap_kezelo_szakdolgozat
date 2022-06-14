package com.giganet.giganet_worksheet.View;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

public class OptionDialog extends Dialog implements  View.OnClickListener {
    private EditText alarmEditText, documentationBackEndEditText, backEndEditText, authenticationBackEndEditText;
    private Button alarmSetButton, patchNoteButton, backEndSet, documentationBackEndSet, authenticationBackEndSet, cancelButton;

    public OptionDialog(@NonNull Context context) {
        super(context);
        setDialog();
        show();
        SharedPreferences sharedPreferences = new SharedPreference(getContext()).getSharedPreferences();
        init(sharedPreferences);
        setListeners();

    }

    private void setDialog(){
        setCanceledOnTouchOutside(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.dialog_options);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void init(android.content.SharedPreferences sharedPreference){
        alarmEditText = findViewById(R.id.et_alarm);
        backEndEditText = findViewById(R.id.et_back_end);
        documentationBackEndEditText = findViewById(R.id.et_documentation);
        authenticationBackEndEditText = findViewById(R.id.et_authentication);

        alarmSetButton = findViewById(R.id.b_alarm_set);
        patchNoteButton = findViewById(R.id.b_patch_notes);
        backEndSet = findViewById(R.id.b_backend_set);
        documentationBackEndSet = findViewById(R.id.b_documentation_set);
        authenticationBackEndSet = findViewById(R.id.b_authentication_set);
        cancelButton = findViewById(R.id.b_close_options);


        alarmEditText.setText(String.format("%d", sharedPreference.getInt(ServiceConstants.Options.ALARMTIME, ServiceConstants.Options.DEFAULT_ALARM_TIME)));
        backEndEditText.setText(String.format("%s", sharedPreference.getString(ServiceConstants.Options.BACKEND, "")));
        documentationBackEndEditText.setText(String.format("%s", sharedPreference.getString(ServiceConstants.Options.DOCUMENTATION, "")));
        authenticationBackEndEditText.setText(String.format("%s", sharedPreference.getString(ServiceConstants.Options.AUTHENTICATION, "")));
    }

    private void setListeners(){
        alarmSetButton.setOnClickListener(this);
        patchNoteButton.setOnClickListener(this);
        backEndSet.setOnClickListener(this);
        documentationBackEndSet.setOnClickListener(this);
        authenticationBackEndSet.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    private void setAlarmTimer() {
        if (alarmEditText.getText().toString() != null && alarmEditText.getText().toString().length() != 0) {
            int alarmTimer = Integer.parseInt(alarmEditText.getText().toString());
            if (alarmTimer > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(getContext()).getSharedPreferences().edit();
                editor.putInt(ServiceConstants.Options.ALARMTIME, alarmTimer);
                editor.apply();
            } else {
                Toast.makeText(getContext(), "Mindenképpen pozitív számnak kell lennie az időzítőnek", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setBackEnd() {
        if (backEndEditText.getText().toString() != null && backEndEditText.getText().toString().length() != 0) {
            String backEndServer = backEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(getContext()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.BACKEND, backEndServer);
                editor.apply();
            }
        }
    }

    private void setDocumentation() {
        if (documentationBackEndEditText.getText().toString() != null && documentationBackEndEditText.getText().toString().length() != 0) {
            String backEndServer = documentationBackEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(getContext()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.DOCUMENTATION, backEndServer);
                editor.apply();
            }
        }
    }

    private void setAuthentication() {
        if (authenticationBackEndEditText.getText().toString() != null && authenticationBackEndEditText.getText().toString().length() != 0) {
            String backEndServer = authenticationBackEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(getContext()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.AUTHENTICATION, backEndServer);
                editor.apply();
            }
        }
    }

    private void showRestartNeeded(){
        Toast.makeText(getContext(),"A beállítások életbe lépéséhez újraindítás szükséges",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.b_alarm_set){
            setAlarmTimer();
            showRestartNeeded();
        } else if( id == R.id.b_backend_set){
            setBackEnd();
            showRestartNeeded();
        } else if (id == R.id.b_documentation_set){
            setDocumentation();
            showRestartNeeded();
        } else if (id == R.id.b_authentication_set){
            setAuthentication();
            showRestartNeeded();
        } else if(id == R.id.b_patch_notes){
            TextDialogs dialog = new TextDialogs(getContext());
            dialog.showPatchNotes();
        } else if(id == R.id.b_close_options){
            this.dismiss();
        }
    }
}
