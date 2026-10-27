package com.deep.skill_drill.dto;

public class OAuthRequest {
    private String idToken;

    public OAuthRequest() {}
    public OAuthRequest(String idToken) { this.idToken = idToken; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}