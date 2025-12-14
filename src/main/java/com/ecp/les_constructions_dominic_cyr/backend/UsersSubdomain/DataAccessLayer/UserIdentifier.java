package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserIdentifier implements Serializable {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    protected UserIdentifier() {
    }

    private UserIdentifier(UUID userId) {
        this.userId = userId;
    }

    public static UserIdentifier newId() {
        return new UserIdentifier(UUID.randomUUID());
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserIdentifier that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return userId != null ? userId.toString() : null;
    }
}
