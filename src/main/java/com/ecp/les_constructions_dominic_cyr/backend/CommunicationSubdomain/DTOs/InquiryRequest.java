package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    private String phone;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    private String recaptchaToken;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRecaptchaToken() { return recaptchaToken; }
    public void setRecaptchaToken(String recaptchaToken) { this.recaptchaToken = recaptchaToken; }
}
