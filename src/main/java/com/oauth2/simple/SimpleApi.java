package com.oauth2.simple;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

@RestController
public class SimpleApi {

    @GetMapping("/user")
    public Map<String,Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name",principal.getAttribute("name"));
    }
    @GetMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }
}
