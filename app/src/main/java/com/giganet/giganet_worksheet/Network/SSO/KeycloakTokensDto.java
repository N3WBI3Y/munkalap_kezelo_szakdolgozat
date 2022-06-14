package com.giganet.giganet_worksheet.Network.SSO;

import com.google.gson.annotations.SerializedName;

public class KeycloakTokensDto {

    @SerializedName("access_token")
    private final String accessToken;
    @SerializedName("expires_in")
    private final Integer expiresIn;
    @SerializedName("refresh_expires_in")
    private final Integer refreshExpiresIn;
    @SerializedName("refresh_token")
    private final String refreshToken;
    @SerializedName("token_type")
    private final String tokenType;
    @SerializedName("id_token")
    private final String idToken;
    @SerializedName("not-before-policy")
    private final String notBeforePolicy;
    @SerializedName("session_state")
    private final String sessionState;
    @SerializedName("scope")
    private final String scope;

    public KeycloakTokensDto(String accessToken, Integer expiresIn, Integer refreshExpiresIn, String refreshToken,
                             String tokenType, String idToken, String notBeforePolicy,
                             String sessionState, String scope) {

        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.idToken = idToken;
        this.notBeforePolicy = notBeforePolicy;
        this.sessionState = sessionState;
        this.scope = scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getNotBeforePolicy() {
        return notBeforePolicy;
    }

    public String getSessionState() {
        return sessionState;
    }

    public String getScope() {
        return scope;
    }
}
