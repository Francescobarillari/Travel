package it.unical.ea.Travel.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Component
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    private static final Logger logger = LoggerFactory.getLogger("security-audit");

    public RequestLoggingFilter() {
        setIncludeQueryString(true);
        setIncludePayload(true);
        setMaxPayloadLength(1000);
        setIncludeHeaders(false);
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/api/") || uri.startsWith("/user") || uri.startsWith("/activity") || uri.startsWith("/itinerary");
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        logger.info("REQUEST_START: method={}, uri={}, user={}", 
                request.getMethod(), 
                request.getRequestURI(), 
                SecurityUtils.getCurrentUserEmail());
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        logger.info("REQUEST_END: method={}, uri={}, user={}", 
                request.getMethod(), 
                request.getRequestURI(), 
                SecurityUtils.getCurrentUserEmail());
    }
}
