package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.OAuth2UserInfo;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import com.dealharbor.dealharbor_backend.security.OAuth2UserInfoFactory;
import com.dealharbor.dealharbor_backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        log.info("Processing OAuth2 login for provider: {}", registrationId);
        
        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(oAuth2UserRequest);
            log.info("Successfully loaded user info from provider: {}", registrationId);
        } catch (Exception ex) {
            log.error("Failed to load user from OAuth2 provider {}: {}", registrationId, ex.getMessage(), ex);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("user_load_failed", "Failed to load user from " + registrationId + ": " + ex.getMessage(), null),
                ex
            );
        }

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Failed to process OAuth2 user for {}: {}", registrationId, ex.getMessage(), ex);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("authentication_failed", ex.getMessage(), null),
                ex
            );
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oAuth2User.getAttributes()
        );

        // GitHub sometimes omits email in /user unless it's public; fetch from /user/emails if missing
        if ("github".equalsIgnoreCase(registrationId) && !StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            String ghEmail = fetchGithubPrimaryEmail(oAuth2UserRequest);
            if (StringUtils.hasText(ghEmail)) {
                HashMap<String, Object> attrs = new HashMap<>(oAuth2User.getAttributes());
                attrs.put("email", ghEmail);
                oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attrs);
            }
        }

        if(!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase())) {
                throw new RuntimeException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .profilePhotoUrl(oAuth2UserInfo.getImageUrl())
                .provider(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase())
                .passwordHash("") // OAuth users don't have passwords
                .role(UserRole.USER)
                .enabled(true)
                .emailVerified(true)
                .locked(false)
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if ("GOOGLE".equals(user.getProvider())) {
            user.setGoogleId(oAuth2UserInfo.getId());
        } else if ("GITHUB".equals(user.getProvider())) {
            user.setGithubId(oAuth2UserInfo.getId());
        }

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setProfilePhotoUrl(oAuth2UserInfo.getImageUrl());
        existingUser.setUpdatedAt(Instant.now());
        return userRepository.save(existingUser);
    }

    private String fetchGithubPrimaryEmail(OAuth2UserRequest request) {
        try {
        String token = request.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<java.util.List<java.util.Map<String, Object>>> resp = restTemplate.exchange(
            "https://api.github.com/user/emails",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {}
        );
        java.util.List<java.util.Map<String, Object>> list = resp.getBody();
            if (list == null) return null;
            String first = null;
            for (java.util.Map<String, Object> emailObj : list) {
                Object email = emailObj.get("email");
                Object primary = emailObj.get("primary");
                Object verified = emailObj.get("verified");
                if (email instanceof String em) {
                    if (first == null) first = em;
                    boolean isPrimary = primary instanceof Boolean b && b;
                    boolean isVerified = verified instanceof Boolean b2 && b2;
                    if (isPrimary && isVerified) {
                        return em;
                    }
                }
            }
            return first; // fallback to first if no primary verified
        } catch (Exception ignored) {
            return null;
        }
    }
}
