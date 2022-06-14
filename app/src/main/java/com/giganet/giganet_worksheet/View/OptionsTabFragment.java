package com.giganet.giganet_worksheet.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.giganet_worksheet.R;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Utils.SharedPreference;


public class OptionsTabFragment extends Fragment implements View.OnClickListener {


    private EditText alarmEditText, documentationBackEndEditText, backEndEditText, authenticationBackEndEditText;
    private Button alarmSetButton, patchNoteButton, backEndSet, documentationBackEndSet, authenticationBackEndSet;

    public OptionsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_options_tab, container, false);;
        android.content.SharedPreferences sharedPreferences = new SharedPreference(requireActivity()).getSharedPreferences();
        init(root, sharedPreferences);
        setListeners();

        return root;
    }

    private void init(View v, android.content.SharedPreferences sharedPreference){
        alarmEditText = v.findViewById(R.id.et_alarm);
        backEndEditText = v.findViewById(R.id.et_back_end);
        documentationBackEndEditText = v.findViewById(R.id.et_documentation);
        authenticationBackEndEditText = v.findViewById(R.id.et_authentication);

        alarmSetButton = v.findViewById(R.id.b_alarm_set);
        patchNoteButton = v.findViewById(R.id.b_patch_notes);
        backEndSet = v.findViewById(R.id.b_backend_set);
        documentationBackEndSet = v.findViewById(R.id.b_documentation_set);
        authenticationBackEndSet = v.findViewById(R.id.b_authentication_set);


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

    }

    private void setAlarmTimer() {
        if (alarmEditText.getText().toString() != null && alarmEditText.getText().toString().length() != 0) {
            int alarmTimer = Integer.parseInt(alarmEditText.getText().toString());
            if (alarmTimer > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(requireActivity()).getSharedPreferences().edit();
                editor.putInt(ServiceConstants.Options.ALARMTIME, alarmTimer);
                editor.apply();
            } else {
                Toast.makeText(requireActivity(), "Mindenképpen pozitív számnak kell lennie az időzítőnek", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setBackEnd() {
        if (backEndEditText.getText().toString() != null && backEndEditText.getText().toString().length() != 0) {
            String backEndServer = backEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(requireActivity()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.BACKEND, backEndServer);
                editor.apply();
            }
        }
    }

    private void setDocumentation() {
        if (documentationBackEndEditText.getText().toString() != null && documentationBackEndEditText.getText().toString().length() != 0) {
            String backEndServer = documentationBackEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(requireActivity()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.DOCUMENTATION, backEndServer);
                editor.apply();
            }
        }
    }

    private void setAuthentication() {
        if (authenticationBackEndEditText.getText().toString() != null && authenticationBackEndEditText.getText().toString().length() != 0) {
            String backEndServer = authenticationBackEndEditText.getText().toString();
            if (backEndServer.length() > 0) {
                android.content.SharedPreferences.Editor editor = new SharedPreference(requireActivity()).getSharedPreferences().edit();
                editor.putString(ServiceConstants.Options.AUTHENTICATION, backEndServer);
                editor.apply();
            }
        }
    }

    private void showRestartNeeded(){
        Toast.makeText(requireActivity(),"A beállítások életbe lépéséhez újraindítás szükséges",Toast.LENGTH_LONG).show();
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
            TextDialogs dialog = new TextDialogs(requireActivity());
            dialog.showPatchNotes();
        }
    }
}