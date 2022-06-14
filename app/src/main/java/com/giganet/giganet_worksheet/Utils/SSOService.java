package com.giganet.giganet_worksheet.Utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.giganet.giganet_worksheet.Network.SSO.CertsDto;
import com.giganet.giganet_worksheet.Network.SSO.KeycloakTokensDto;
import com.giganet.giganet_worksheet.Network.SSO.RetrofitClientSSO;
import com.giganet.giganet_worksheet.Resources.Events.SSOResponseEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SSOService {
    private static CompletableFuture<String> requestToken(String username, String password, Context context) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                Response<CertsDto> certs = RetrofitClientSSO.getInstance(context).getApi().getCerts().execute();
                if (certs.code() == 200 && certs.body() != null && certs.body().toString().length() > 0) {
                    Response<KeycloakTokensDto> tokenCall = RetrofitClientSSO.getInstance(context).getApi().getAccessToken("worksheet-android", "password"
                            , "RKxkTrJgxc4D5qvSzFSNk2rngc537uuX", "openid", username, password).execute();


                    if (tokenCall.code() == 200 && tokenCall.body() != null) {
                        result.complete(processToken(username, tokenCall.body(), certs.body(), context));
                        return;
                    } else if (tokenCall.code() == 403) {
                        result.complete("forbidden");
                        return;
                    }
                } else {
                    result.complete("");
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                result.complete("");
            }
            result.complete("");

        });
        return result;
    }

    public static void getToken(Context context, SSOResult ssoResult) {
        android.content.SharedPreferences sharedPreference = new EncryptedSharedPreference(context).getEncryptedSharedPreference();
        String username = sharedPreference.getString("EncryptedUsername", "");
        String password = sharedPreference.getString("EncryptedPassword", "");
        getToken(username, password, context, ssoResult);
    }

    public static void getToken(String username, String password, Context context, SSOResult ssoResult) {
        String token;
        android.content.SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
        if (!sharedPreferences.getString("accessToken", "").equals("")) {
            if (!isAccessTokenExpired(context)) {
                token = sharedPreferences.getString("accessToken", "");
                ssoResult.onSuccess(token);
                EventBus.getDefault().postSticky(new SSOResponseEvent(true));
                return;
            }
        }
        if (isRefreshTokenExpired(context)) {
            try {
                token = requestToken(username, password, context).get();
                returnToken(token, ssoResult);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                token = refreshToken(context).get();
                returnToken(token, ssoResult);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void logoutFromSSO(String accessToken, Context context) {
        SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
        String refreshToken = sharedPreferences.getString("refreshToken", "");
        if (!refreshToken.equals("")) {
            RetrofitClientSSO.getInstance(context).getApi().logout(accessToken, "worksheet-android"
                    , "RKxkTrJgxc4D5qvSzFSNk2rngc537uuX", refreshToken, "openid").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("accessToken");
                    editor.remove("refreshToken");
                    editor.remove("accessTokenExpiration");
                    editor.remove("refreshTokenExpiration");
                    editor.remove("realName");
                    editor.remove("prefferedUsername");
                    editor.apply();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        }
    }

    private static void returnToken(String token, SSOResult ssoResult) {
        if (token.equals("forbidden")) {
            ssoResult.onFailure(token);
            EventBus.getDefault().postSticky(new SSOResponseEvent(false));
            return;
        }

        if (token.length() > 0) {
            ssoResult.onSuccess(token);
            EventBus.getDefault().postSticky(new SSOResponseEvent(true));
        } else {
            ssoResult.onFailure("");
            EventBus.getDefault().postSticky(new SSOResponseEvent(false));
        }
    }

    private static boolean isAccessTokenExpired(Context context) {
        android.content.SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
        android.content.SharedPreferences encryptedPreferences = new EncryptedSharedPreference(context).getEncryptedSharedPreference();
        if (!sharedPreferences.getString("tokenUser", "a").equals(encryptedPreferences.getString("EncryptedUsername", ""))) {
            return true;
        }
        return sharedPreferences.getLong("accessTokenExpiration", 0) - new Date().getTime() < 15000;
    }

    private static boolean isRefreshTokenExpired(Context context) {
        android.content.SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
        android.content.SharedPreferences encryptedPreferences = new EncryptedSharedPreference(context).getEncryptedSharedPreference();
        if (!sharedPreferences.getString("tokenUser", "a").equals(encryptedPreferences.getString("EncryptedUsername", ""))) {
            return true;
        }
        return sharedPreferences.getLong("refreshTokenExpiration", 0) - new Date().getTime() < 15000;
    }

    private static PublicKey getPublicKey(String base64PublicKey) throws CertificateException {
        //https://community.auth0.com/t/problem-validating-jwt-with-x5c-public-key-from-microsoft/57596
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(keyBytes));
        return cer.getPublicKey();
    }

    private static CompletableFuture<String> refreshToken(Context context) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            try {
                Response<CertsDto> certs = RetrofitClientSSO.getInstance(context).getApi().getCerts().execute();

                if (certs.code() == 200 && certs.body() != null && certs.body().toString().length() > 0) {
                    SharedPreferences sharedPreferences = new SharedPreference(context).getSharedPreferences();
                    String refreshToken = sharedPreferences.getString("refreshToken", "");
                    String username = sharedPreferences.getString("USERNAME", "");
                    Response<KeycloakTokensDto> tokenCall = RetrofitClientSSO.getInstance(context).getApi().refreshToken("worksheet-android"
                            , "refresh_token", "RKxkTrJgxc4D5qvSzFSNk2rngc537uuX", "openid", refreshToken).execute();


                    if (tokenCall.code() == 200 && tokenCall.body() != null) {
                        result.complete(processToken(username, tokenCall.body(), certs.body(), context));
                        return;
                    } else if (tokenCall.code() == 403) {
                        result.complete("forbidden");
                        return;
                    }
                } else {
                    result.complete("");
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                result.complete("");
            }
            result.complete("");

        });
        return result;
    }

    private static String processToken(String username, KeycloakTokensDto token, CertsDto certs, Context context) {
        try {
            String tokenKId = getKIdFromToken(token.getAccessToken());
            String tokenCert = getTokenCert(certs, tokenKId);
            PublicKey publicKey = getPublicKey(tokenCert);
            Jws<Claims> verifiedJWT = verifyJWT(token.getAccessToken(), publicKey);
            if (verifiedJWT == null) {
                return "";
            }
            SharedPreferences.Editor sharedPreference = new SharedPreference(context).getSharedPreferences().edit();
            sharedPreference.putString("tokenUser", username);
            sharedPreference.putString("accessToken", token.getTokenType() + " " + token.getAccessToken());
            sharedPreference.putString("refreshToken", token.getRefreshToken());
            sharedPreference.putLong("accessTokenExpiration", new Date().getTime() + token.getExpiresIn() * 1000);
            sharedPreference.putLong("refreshTokenExpiration", new Date().getTime() + token.getRefreshExpiresIn() * 1000);
            sharedPreference.putString("realName", verifiedJWT.getBody().get("name").toString());
            sharedPreference.putString("prefferedUsername", verifiedJWT.getBody().get("preferred_username").toString());
            sharedPreference.apply();

        } catch (CertificateException | JwtException e) {
            e.printStackTrace();
            return "";
        }
        return token.getTokenType() + " " + token.getAccessToken();
    }


    private static String getKIdFromToken(String token) {
        if (token != null && token.length() > 0) {
            String headerEncoded = token.split("\\.")[0];
            byte[] headerBytes = Base64.getDecoder().decode(headerEncoded);
            try {
                String header = new String(headerBytes, "UTF8");

                JsonParser parser = new JsonParser();
                JsonObject headerJsonObject = (JsonObject) parser.parse(header);
                return headerJsonObject.get("kid").getAsString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    private static String getTokenCert(CertsDto certs, String kid) {
        String result = "";
        for (CertsDto.Key cert : certs.getKeys()) {
            if (cert.getkId().equals(kid)) {
                result = cert.getX5c().get(0);
                return result;
            }
        }
        return result;
    }

    private static Jws<Claims> verifyJWT(String accessToken, PublicKey publicKey) {
        Jws<Claims> jws = null;
        jws = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(accessToken);


        return jws;
    }
}
