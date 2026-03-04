package com.example.ecommerce.auth.service;

import com.example.ecommerce.common.util.RequestUtils;
import com.example.ecommerce.user.entity.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
public class AuthAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void loginSuccess(UUID userId, String email, RoleName role) {
        log("AUTH_LOGIN_SUCCESS", userId, email, role, null);
    }

    public void loginFailure(String email, RoleName role, String reason) {
        log("AUTH_LOGIN_FAILURE", null, email, role, reason);
    }

    public void registerSuccess(UUID userId, String email, RoleName role) {
        log("AUTH_REGISTER_SUCCESS", userId, email, role, null);
    }

    public void registerFailure(String email, RoleName role, String reason) {
        log("AUTH_REGISTER_FAILURE", null, email, role, reason);
    }

    public void setupAdminSuccess(UUID userId, String email) {
        log("AUTH_SETUP_ADMIN_SUCCESS", userId, email, RoleName.ROLE_ADMIN, null);
    }

    public void setupAdminFailure(String email, String reason) {
        log("AUTH_SETUP_ADMIN_FAILURE", null, email, RoleName.ROLE_ADMIN, reason);
    }

    private void log(String event, UUID userId, String email, RoleName role, String reason) {
        HttpServletRequest request = currentRequest();
        String ip = RequestUtils.clientIp(request);
        String ua = request != null ? request.getHeader("User-Agent") : null;
        String requestId = request != null ? request.getHeader("X-Request-Id") : null;

        log.info("{} userId={} email={} role={} ip={} ua={} requestId={} reason={}",
                event,
                userId,
                safe(email),
                role,
                safe(ip),
                safe(ua),
                safe(requestId),
                safe(reason));
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
