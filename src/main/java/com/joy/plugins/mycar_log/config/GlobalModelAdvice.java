package com.joy.plugins.mycar_log.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loggedInUserName")
    public String loggedInUserName(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        String displayName = principal.getAttribute("displayName");
        if (displayName != null) {
            return displayName;
        }
        String email = principal.getAttribute("email");
        if (email != null) {
            return email;
        }
        return principal.getName();
    }
}
