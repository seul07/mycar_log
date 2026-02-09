package com.joy.plugins.mycar_log.util;

import org.springframework.security.oauth2.core.user.OAuth2User;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static String getOauthId(OAuth2User principal) {
        return principal.getAttribute("oauthId");
    }
}
