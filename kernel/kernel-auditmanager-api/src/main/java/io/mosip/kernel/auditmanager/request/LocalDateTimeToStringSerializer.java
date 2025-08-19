package io.mosip.kernel.auditmanager.request;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeToStringSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String isoString = value.atZone(ZoneId.of("UTC")).toInstant().toString();
        gen.writeString(isoString); // produces 2025-08-19T07:40:49.966588424Z
    }
}
