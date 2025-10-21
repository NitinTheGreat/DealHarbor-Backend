package com.dealharbor.dealharbor_backend.security;

import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import com.dealharbor.dealharbor_backend.services.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SecurityService securityService;

    @Value("${app.oauth2.authorizedRedirectUri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    try {
        final String provider = (authentication instanceof OAuth2AuthenticationToken token)
            ? token.getAuthorizedClientRegistrationId().toUpperCase()
            : "OAUTH2";

        String email = authentication.getName();

        // Our CustomOAuth2UserService returns UserPrincipal with username=email
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> securityService.recordSecurityEvent(
            user.getId(),
            "LOGIN",
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            "Login via " + provider
        ));
    } catch (Exception ignored) {}

        setAlwaysUseDefaultTargetUrl(true);
        setDefaultTargetUrl(redirectUri);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
