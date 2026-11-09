package com.refactorlab.api.security;

import com.refactorlab.api.rest.RefactorLabErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

// Güvenlik hatalarını da ortak JSON hata modeliyle döner:
//   401 -> kimlik bilgisi yok / yanlış
//   403 -> kimlik doğru ama rol yetersiz
@Component
public class RefactorLabSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public RefactorLabSecurityErrorHandler(JsonMapper theJsonMapper) {
        jsonMapper = theJsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"RefactorLab\"");
        writeError(response, HttpStatus.UNAUTHORIZED, "Authentication required - missing or invalid credentials");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeError(response, HttpStatus.FORBIDDEN,
                "Access denied - you do not have permission for " + request.getMethod() + " " + request.getRequestURI());
    }

    private void writeError(HttpServletResponse response, HttpStatus theStatus, String theMessage) throws IOException {
        RefactorLabErrorResponse theError = new RefactorLabErrorResponse(theStatus.value(), theMessage, System.currentTimeMillis());
        response.setStatus(theStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonMapper.writeValueAsString(theError));
    }
}
