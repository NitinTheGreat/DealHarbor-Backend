package com.dealharbor.dealharbor_backend.dto;

import java.util.Map;

public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        // GitHub may return null for email unless scope user:email and email is public.
        Object email = attributes.get("email");
        if (email instanceof String s && s != null && !s.isEmpty()) {
            return s;
        }
        // Try primary email if available via attributes (some providers include it in userinfo extensions)
        Object primaryEmail = attributes.get("primary_email");
        if (primaryEmail instanceof String ps && ps != null && !ps.isEmpty()) {
            return ps;
        }
        return null; // Let upper layer handle missing email (will throw with clear message)
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
