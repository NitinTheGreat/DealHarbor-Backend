package com.dealharbor.dealharbor_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorizedRedirectUri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        
        // Extract the actual error message from OAuth2AuthenticationException
        String errorMessage;
        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            // Get the error description from OAuth2Error
            errorMessage = oauthEx.getError().getDescription();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = oauthEx.getError().getErrorCode();
            }
            log.error("OAuth2 error code: {}, description: {}", 
                oauthEx.getError().getErrorCode(), 
                oauthEx.getError().getDescription());
        } else {
            errorMessage = exception.getMessage();
        }
        
        // Get the root cause for even better error messages
        Throwable cause = exception.getCause();
        if (cause != null) {
            log.error("Root cause: {}", cause.getMessage(), cause);
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = cause.getMessage();
            }
        }
        
        log.error("OAuth2 failure details - URI: {}, Final Error: {}", 
            request.getRequestURI(), errorMessage);
        
        // Redirect to frontend with error information
        String targetUrl = redirectUri + "?error=" + URLEncoder.encode(
            errorMessage != null ? errorMessage : "Authentication failed", 
            StandardCharsets.UTF_8
        );
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
