package com.giganet.giganet_worksheet.Model;

import android.content.Context;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.AuthenticationContract;
import com.giganet.giganet_worksheet.Network.Documentation.DocumentationStatusDto;
import com.giganet.giganet_worksheet.Network.Documentation.RetrofitClientDocumentation;
import com.giganet.giganet_worksheet.Utils.SSOResult;
import com.giganet.giganet_worksheet.Network.Worksheet.MaterialDto;
import com.giganet.giganet_worksheet.Network.Worksheet.RetrofitClientWorksheet;
import com.giganet.giganet_worksheet.Network.Worksheet.ServiceTypeDto;
import com.giganet.giganet_worksheet.Persistence.Documentation.DocumentationStatusTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationMaterialTableHandler;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationServiceTypesTableHandler;
import com.giganet.giganet_worksheet.Utils.SSOService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class AuthenticationModel implements AuthenticationContract.Model {

    private final String username;
    private final String password;

    public AuthenticationModel(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void savePreference(SharedPreferences.Editor editor, String username, boolean rememberMe) {
        editor.putString("USERNAME", username);
        editor.putBoolean("REMEMBERME", rememberMe);
        editor.apply();
        saveEncryptedUser(editor);
    }

    @Override
    public void saveEncryptedUser(SharedPreferences.Editor editor) {
        editor.putString("EncryptedUsername", username);
        editor.putString("EncryptedPassword", password);
        editor.apply();
    }

    @Override
    public boolean isValidPassword() {
        return this.password != null && this.password.length() > 0;
    }


    @Override
    public boolean isValidUsername() {
        return this.username != null && this.username.length() > 0;
    }


    @Override
    public String getWelcomeMessage() {
        return "Üdvözöllek:\n" + username;
    }

    @Override
    public void syncTypesWithBackEnds(Context activity) {
        Executors.newSingleThreadExecutor().submit(() -> {
            syncDocumentationTypesWithBackEnd(activity);
        });
        Executors.newSingleThreadExecutor().submit(() -> syncServiceTypesWithBackEnd(activity));
        Executors.newSingleThreadExecutor().submit(() -> syncMaterialsWithBackEnd(activity));
    }

    private void syncDocumentationTypesWithBackEnd(Context activity){
        SSOService.getToken(activity, new SSOResult() {
            @Override
            public void onSuccess(String token) {
                Response<ArrayList<DocumentationStatusDto>> response;
                try {
                    response = RetrofitClientDocumentation.getInstance(activity).getApi().getStatusTypes(token).execute();
                    if (response.code() == 200 && response.body() != null) {
                        DocumentationStatusTableHandler db = new DocumentationStatusTableHandler(activity.getApplicationContext());
                        db.addOrUpdateTypes(response.body(),username);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String token) {

            }
        });
    }

    private void syncServiceTypesWithBackEnd(Context activity) {
        SSOService.getToken(activity, new SSOResult() {
            @Override
            public void onSuccess(String token) {
                try {
                    Response<List<ServiceTypeDto>> response = RetrofitClientWorksheet.getInstance(activity).getApi().getServiceTypeActions(token).execute();
                    if (response.code() == 200 && response.body() != null) {
                        InstallationServiceTypesTableHandler tableHandler = new InstallationServiceTypesTableHandler(activity);
                        tableHandler.clearDb();
                        List<ServiceTypeDto> serviceTypeEntities = response.body();
                        for (ServiceTypeDto entity : serviceTypeEntities) {
                            String serviceJson = new Gson().toJson(entity.getActionList());
                            tableHandler.addService(entity.getName(), serviceJson);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String token) {
            }
        });
    }

    private void syncMaterialsWithBackEnd(Context activity){
        SSOService.getToken(activity, new SSOResult() {
            @Override
            public void onSuccess(String token) {
                try {
                    Response<List<MaterialDto>> response = RetrofitClientWorksheet.getInstance(activity).getApi().getMaterials(token).execute();
                    if (response.code() == 200 && response.body() != null){
                        InstallationMaterialTableHandler materialTableHandler = new InstallationMaterialTableHandler(activity);
                        materialTableHandler.clearDb();
                        List<MaterialDto> materialEntities = response.body();
                        for (MaterialDto entity : materialEntities) {
                            String serviceJson = new Gson().toJson(entity.getMaterialList());
                            materialTableHandler.addMaterial(entity.getName(),serviceJson);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String token) {
            }
        });
    }
}
