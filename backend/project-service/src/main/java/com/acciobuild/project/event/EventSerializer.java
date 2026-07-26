package com.acciobuild.project.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

/**
 * Utility serializing and deserializing event payloads to and from JSON formats.
 */
@Component
public class EventSerializer {

    private final ObjectMapper objectMapper;

    /**
     * Initializes the serializer with a JavaTimeModule for LocalDateTimes serialization support.
     */
    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule());
    }

    /**
     * Serializes any event payload object to a JSON string.
     */
    public String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize domain event: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string back into the target class.
     */
    public <T> T deserialize(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize domain event: " + e.getMessage(), e);
        }
    }
}
