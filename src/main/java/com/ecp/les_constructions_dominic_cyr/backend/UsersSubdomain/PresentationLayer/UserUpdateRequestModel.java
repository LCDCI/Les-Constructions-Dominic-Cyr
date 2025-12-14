package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer;

import jakarta.validation.constraints.Email;

public class UserUpdateRequestModel {

    private String firstName;
    private String lastName;
    private String phone;
    
    @Email(message = "Secondary email must be a valid email address")
    private String secondaryEmail;

    public UserUpdateRequestModel() {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }
}
