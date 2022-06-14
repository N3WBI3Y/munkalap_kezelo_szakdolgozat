package com.giganet.giganet_worksheet.Network.SSO;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SSOApi {


    @GET("auth/realms/GIGANET/protocol/openid-connect/certs")
    Call<CertsDto> getCerts();

    @FormUrlEncoded
    @POST("auth/realms/GIGANET/protocol/openid-connect/token")
    Call<KeycloakTokensDto> getAccessToken(@Field("client_id") String clientId,
                                           @Field("grant_type") String grantType,
                                           @Field("client_secret") String clientSecret,
                                           @Field("scope") String scope,
                                           @Field("username") String username,
                                           @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/realms/GIGANET/protocol/openid-connect/token")
    Call<KeycloakTokensDto> refreshToken(@Field("client_id") String clientId,
                                         @Field("grant_type") String grantType,
                                         @Field("client_secret") String clientSecret,
                                         @Field("scope") String scope,
                                         @Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("auth/realms/GIGANET/protocol/openid-connect/logout")
    Call<Void> logout(@Header("Authorization") String token,
                      @Field("client_id") String clientId,
                      @Field("client_secret") String clientSecret,
                      @Field("refresh_token") String refreshToken,
                      @Field("scope") String scope);

}
