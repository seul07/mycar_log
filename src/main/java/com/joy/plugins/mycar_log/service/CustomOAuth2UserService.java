package com.joy.plugins.mycar_log.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String oauthId;
        String email = null;
        String displayName = null;

        if ("google".equals(registrationId)) {
            oauthId = "google_" + oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            displayName = oAuth2User.getAttribute("name");
        } else if ("kakao".equals(registrationId)) {
            oauthId = "kakao_" + oAuth2User.getAttribute("id");

            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    displayName = (String) profile.get("nickname");
                }
            }
        } else {
            oauthId = registrationId + "_" + oAuth2User.getName();
        }

        userService.getOrCreateOAuthUser(oauthId, email, displayName);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("oauthId", oauthId);
        attributes.put("displayName", displayName);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                userNameAttributeName
        );
    }
}
