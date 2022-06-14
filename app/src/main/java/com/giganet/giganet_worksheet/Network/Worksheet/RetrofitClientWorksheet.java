package com.giganet.giganet_worksheet.Network.Worksheet;

import android.content.Context;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.Network.RetrofitClient;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientWorksheet extends RetrofitClient {

    private static RetrofitClientWorksheet instance = null;
    private final WorksheetApi api;
    OkHttpClient okHttpClient;

    private RetrofitClientWorksheet(String url) {
        okHttpClient = createOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(WorksheetApi.class);

    }

    public static synchronized RetrofitClientWorksheet getInstance(Context context) {
        if (instance == null) {
            SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
            String url = sharedPreferences.getString(ServiceConstants.Options.BACKEND,ServiceConstants.Options.DEFAULT_BACKEND);
            instance = new RetrofitClientWorksheet(url);
        }
        return instance;
    }

    public WorksheetApi getApi() {
        return api;
    }
}
