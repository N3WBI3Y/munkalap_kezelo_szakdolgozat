package com.giganet.giganet_worksheet.Network.Documentation;

import android.content.Context;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.Network.RetrofitClient;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientDocumentation extends RetrofitClient {

    private static RetrofitClientDocumentation instance = null;
    private final DocumentationApi api;
    OkHttpClient okHttpClient;

    private RetrofitClientDocumentation(String url) {
        okHttpClient = createOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(DocumentationApi.class);

    }

    public static synchronized RetrofitClientDocumentation getInstance(Context context) {
        if (instance == null) {
            SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
            String url = sharedPreferences.getString(ServiceConstants.Options.DOCUMENTATION,ServiceConstants.Options.DEFAULT_DOCUMENTATION);
            instance = new RetrofitClientDocumentation(url);
        }
        return instance;
    }

    public DocumentationApi getApi() {
        return api;
    }

}
