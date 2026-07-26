package com.acciobuild.knowledge.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

/**
 * Jackson based serialization utility supporting date, time, and UUID formats.
 */
@Component
public class EventSerializer {

    private final ObjectMapper objectMapper;

    /**
     * Initializes mapping module configurations.
     */
    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule());
    }

    /**
     * Serializes event objects.
     */
    public String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize knowledge event: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes payloads.
     */
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize knowledge event: " + e.getMessage(), e);
        }
    }
}
