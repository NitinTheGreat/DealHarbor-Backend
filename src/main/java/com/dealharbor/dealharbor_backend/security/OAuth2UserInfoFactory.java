package com.dealharbor.dealharbor_backend.security;

import com.dealharbor.dealharbor_backend.dto.GitHubOAuth2UserInfo;
import com.dealharbor.dealharbor_backend.dto.GoogleOAuth2UserInfo;
import com.dealharbor.dealharbor_backend.dto.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("github")) {
            return new GitHubOAuth2UserInfo(attributes);
        } else {
            throw new RuntimeException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
