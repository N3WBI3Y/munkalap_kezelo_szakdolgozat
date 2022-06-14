package com.giganet.giganet_worksheet.Network.SSO;

import android.content.Context;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.Network.RetrofitClient;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;
import com.giganet.giganet_worksheet.Utils.SharedPreference;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientSSO  extends RetrofitClient {

    private static RetrofitClientSSO instance = null;
    private final SSOApi api;
    OkHttpClient okHttpClient;

    private RetrofitClientSSO(String url) {
        okHttpClient = createOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SSOApi.class);

    }

    public static synchronized RetrofitClientSSO getInstance(Context context) {
        if (instance == null) {
            SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
            String url = sharedPreferences.getString(ServiceConstants.Options.AUTHENTICATION,ServiceConstants.Options.DEFAULT_AUTHENTICATION);
            instance = new RetrofitClientSSO(url);
        }
        return instance;
    }

    public SSOApi getApi() {
        return api;
    }
}