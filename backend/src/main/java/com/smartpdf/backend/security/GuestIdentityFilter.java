
package com.smartpdf.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class GuestIdentityFilter extends OncePerRequestFilter {

    public static final String GUEST_ID_ATTRIBUTE = "GUEST_ID";
    private static final String COOKIE_NAME = "SMART_PDF_GUEST";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String guestId = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    guestId = cookie.getValue();
                    break;
                }
            }
        }

        if (guestId == null || guestId.isBlank()
                || guestId.length() > 100) {

            guestId = UUID.randomUUID().toString();

            response.addHeader(
                    "Set-Cookie",
                    COOKIE_NAME + "=" + guestId
                            + "; Path=/"
                            + "; Max-Age=2592000"
                            + "; HttpOnly"
                            + "; Secure"
                            + "; SameSite=None"
            );
        }

        request.setAttribute(GUEST_ID_ATTRIBUTE, guestId);

        filterChain.doFilter(request, response);
    }
}
