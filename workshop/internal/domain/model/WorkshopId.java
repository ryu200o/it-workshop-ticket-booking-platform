package com.example.itworkshopticketbookingplatform.workshop.internal.domain.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value Object representing Workshop identifier.
 * Immutable and serializable.
 */
public record WorkshopId(UUID id) implements Serializable {

    public WorkshopId {
        if (id == null) {
            throw new IllegalArgumentException("Workshop ID cannot be null");
        }
    }

    /**
     * Creates a new WorkshopId with a random UUID.
     */
    public static WorkshopId create() {
        return new WorkshopId(UUID.randomUUID());
    }

    /**
     * Creates a WorkshopId from a UUID string.
     */
    public static WorkshopId from(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("Workshop ID string cannot be null or empty");
        }
        return new WorkshopId(UUID.fromString(uuidString.trim()));
    }

    /**
     * Creates a WorkshopId from a UUID.
     */
    public static WorkshopId from(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Workshop ID UUID cannot be null");
        }
        return new WorkshopId(uuid);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkshopId that = (WorkshopId) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}