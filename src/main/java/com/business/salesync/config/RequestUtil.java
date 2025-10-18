package com.business.salesync.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("requestUtil")
@RequestScope
public class RequestUtil {

    private final HttpServletRequest request;

    public RequestUtil(HttpServletRequest request) {
        this.request = request;
    }

    public String getUri() {
        try {
            return request.getRequestURI();
        } catch (Exception e) {
            return ""; // safely return empty string if null
        }
    }

    public boolean isActive(String path) {
        try {
            return request.getRequestURI() != null && request.getRequestURI().startsWith(path);
        } catch (Exception e) {
            return false;
        }
    }

    public String currentUri(String path) {
        return isActive(path) ? "active" : "";
    }
}

