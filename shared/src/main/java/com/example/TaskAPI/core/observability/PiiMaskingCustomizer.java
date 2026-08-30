package com.example.TaskAPI.core.observability;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;

import java.util.regex.Pattern;

public class PiiMaskingCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {
    private static final Pattern NRIC = Pattern.compile("[SFTG]\\d{7}[A-Z]");

    @Override
    public void customize(JsonWriter.Members<Object> members) {
        members.add("message", (event) -> maskNric(String.valueOf(event)));
    }

    private String maskNric(String message) {
        return NRIC.matcher(message).replaceAll(m -> {
            String match = m.group();

            return match.charAt(0) + "****" + match.substring(5);
        });
    }
}
