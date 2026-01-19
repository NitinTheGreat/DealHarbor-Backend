package com.dealharbor.dealharbor_backend.security;

import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import com.dealharbor.dealharbor_backend.services.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
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

            // Extract email from UserPrincipal
            String email = null;
            if (authentication.getPrincipal() instanceof UserPrincipal up) {
                email = up.getEmail();
            }

            if (email != null) {
                final String userEmail = email; // Make effectively final for lambda
                Optional<User> userOpt = userRepository.findByEmail(email);
                userOpt.ifPresent(user -> {
                    securityService.recordSecurityEvent(
                        user.getId(),
                        "LOGIN",
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        "Login via " + provider
                    );
                    log.info("OAuth2 login successful for user: {} via {}", userEmail, provider);
                });
            }
        } catch (Exception e) {
            log.error("Error recording OAuth2 login event", e);
        }

        // For session-based auth: redirect with success flag
        // Frontend should then call /api/auth/me to verify the session
        String targetUrl = redirectUri + "?oauth=success";
        log.info("Redirecting to: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

