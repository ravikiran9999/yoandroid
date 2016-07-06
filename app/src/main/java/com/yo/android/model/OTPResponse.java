package com.yo.android.model;

/**
 * Created by creatives on 7/5/2016.
 */
public class OTPResponse {

    private String scope;

    private String created_at;

    private String expires_in;

    private String token_type;

    private String refresh_token;

    private String access_token;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    @Override
    public String toString() {
        return "ClassPojo [scope = " + scope + ", created_at = " + created_at + ", expires_in = " + expires_in + ", token_type = " + token_type + ", refresh_token = " + refresh_token + ", access_token = " + access_token + "]";
    }
}
