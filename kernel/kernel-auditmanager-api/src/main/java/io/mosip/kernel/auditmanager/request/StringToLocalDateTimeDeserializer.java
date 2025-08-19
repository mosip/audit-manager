package io.mosip.kernel.auditmanager.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class StringToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        // Parse ISO string with Z into Instant, then to LocalDateTime in UTC (or any zone)
        Instant instant = Instant.parse(value);
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
