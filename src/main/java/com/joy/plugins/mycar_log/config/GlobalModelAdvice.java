package com.joy.plugins.mycar_log.config;

import com.joy.plugins.mycar_log.entity.User;
import com.joy.plugins.mycar_log.service.UserService;
import com.joy.plugins.mycar_log.util.AuthUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserService userService;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("loggedInUserName")
    public String loggedInUserName(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        String oauthId = AuthUtil.getOauthId(principal);
        Optional<User> user = userService.findByOauthId(oauthId);
        if (user.isPresent() && user.get().getDisplayName() != null) {
            return user.get().getDisplayName();
        }
        String email = principal.getAttribute("email");
        if (email != null) {
            return email;
        }
        return principal.getName();
    }
}
