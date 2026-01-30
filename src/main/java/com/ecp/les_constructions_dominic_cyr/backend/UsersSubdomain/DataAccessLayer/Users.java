package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Users {

    @EmbeddedId
    private UserIdentifier userIdentifier;

    private String firstName;
    private String lastName;

    // Map to existing "email" column for backwards compatibility
    @Column(name = "email")
    private String primaryEmail;

    // New column for secondary email
    @Column(name = "secondary_email")
    private String secondaryEmail;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "auth0user_id")
    private String auth0UserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status")
    private UserStatus userStatus;

    public Users() {
    }

    public Users(UserIdentifier userIdentifier,
                 String firstName,
                 String lastName,
                 String primaryEmail,
                 String secondaryEmail,
                 String phone,
                 UserRole userRole,
                 String auth0UserId,
                 UserStatus userStatus) {
        this.userIdentifier = userIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.primaryEmail = primaryEmail;
        this.secondaryEmail = secondaryEmail;
        this.phone = phone;
        this.userRole = userRole;
        this.auth0UserId = auth0UserId;
        this.userStatus = userStatus;
    }

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getAuth0UserId() {
        return auth0UserId;
    }

    public void setAuth0UserId(String auth0UserId) {
        this.auth0UserId = auth0UserId;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
