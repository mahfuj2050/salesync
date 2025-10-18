package com.business.salesync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class GlobalDateConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(GlobalDateConfig.class);

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDate.class, this::convertToLocalDate);
        registry.addConverter(String.class, LocalDateTime.class, this::convertToLocalDateTime);
        log.info("✅ Global Date Converters Registered (LocalDate + LocalDateTime)");
    }

    private LocalDate convertToLocalDate(String source) {
        if (isEmpty(source)) return null;

        try {
            return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(source, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (DateTimeParseException e2) {
                log.warn("⚠️ Could not parse LocalDate from value '{}'", source);
                return null;
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(String source) {
        if (isEmpty(source)) return null;

        try {
            // yyyy-MM-dd'T'HH:mm (HTML5 datetime-local)
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                // yyyy-MM-dd (no time provided)
                return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    // dd-MM-yyyy (fallback)
                    return LocalDate.parse(source, DateTimeFormatter.ofPattern("dd-MM-yyyy")).atStartOfDay();
                } catch (DateTimeParseException e3) {
                    log.warn("⚠️ Could not parse LocalDateTime from value '{}'", source);
                    return null;
                }
            }
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
