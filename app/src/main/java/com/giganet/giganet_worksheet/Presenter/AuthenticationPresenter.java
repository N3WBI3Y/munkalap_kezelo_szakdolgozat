package com.giganet.giganet_worksheet.Presenter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.giganet.giganet_worksheet.AuthenticationContract;
import com.giganet.giganet_worksheet.Model.AuthenticationModel;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Resources.Services.NewTaskInBackgroundWorker;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.giganet.giganet_worksheet.Utils.EncryptedSharedPreference;
import com.giganet.giganet_worksheet.Utils.NetworkHelper;
import com.giganet.giganet_worksheet.Utils.PermissionContainer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuthenticationPresenter implements AuthenticationContract.Presenter {

    final AuthenticationContract.View view;
    AuthenticationContract.Model model;

    public AuthenticationPresenter(AuthenticationContract.View view) {
        this.view = view;
    }


    @Override
    public void validate(String username, String password, Context context) {
        model = new AuthenticationModel(username, password);
        Executors.newFixedThreadPool(1).submit(() -> {
            if (!model.isValidUsername()) {
                view.onError("Hibás felhasználó név!");
                return;
            }
            if (!model.isValidPassword()) {
                view.onError("Hibás  jelszó!");
                return;
            }

            if (!PermissionContainer.LocationPermission.checkPermission(view.getActivity())) {
                PermissionContainer.LocationPermission.askPermission(view.getActivity());
                view.onError("Ehhez a funkcióhoz engedélyezned kell a helymeghatározót!");
                return;
            }
            if (!PermissionContainer.LocationPermission.checkLocationProvider(view.getActivity())) {
                view.onError("Ehhez a funkcióhoz be kell kapcsolnod a helymeghatározót!");
                return;
            }

            if (NetworkHelper.isNetworkAvailable(context)) {

                SSOService.getToken(username, password, context, new SSOResult() {
                    @Override
                    public void onSuccess(String token) {
                        doLogin(username, password);
                    }

                    @Override
                    public void onFailure(String token) {
                        view.onError("Nem megfelelő jelszó vagy felhasználónév");
                    }
                });
            } else {
                doLogin(username, password);
            }
        });
    }

    @Override
    public void doLogin(String username, String password) {
        model.syncTypesWithBackEnds(view.getActivity());

        view.onSuccess(model.getWelcomeMessage());
        PeriodicWorkRequest newTaskScanner = new PeriodicWorkRequest.Builder(NewTaskInBackgroundWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder().setRequiresCharging(false).setRequiredNetworkType(NetworkType.CONNECTED).build()).build();

        WorkManager.getInstance(view.getActivity()).enqueueUniquePeriodicWork("newTaskScanner", ExistingPeriodicWorkPolicy.KEEP, newTaskScanner);
    }

    @Override
    public void savePreference(SharedPreferences.Editor editor, String username, boolean rememberMe) {
        model.savePreference(editor, username, rememberMe);
        SharedPreferences sharedPreferences = new EncryptedSharedPreference(view.getActivity()).getEncryptedSharedPreference();
        SharedPreferences.Editor encryptedEditor = sharedPreferences.edit();
        model.saveEncryptedUser(encryptedEditor);
    }

}
