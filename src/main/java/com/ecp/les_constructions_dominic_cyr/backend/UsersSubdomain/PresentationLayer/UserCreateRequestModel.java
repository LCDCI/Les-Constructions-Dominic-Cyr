package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;

public class UserCreateRequestModel {

    private String firstName;
    private String lastName;

    // Primary login email
    private String primaryEmail;

    // Secondary contact email
    private String secondaryEmail;

    private String phone;
    private UserRole userRole;

    public UserCreateRequestModel() {
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
}
