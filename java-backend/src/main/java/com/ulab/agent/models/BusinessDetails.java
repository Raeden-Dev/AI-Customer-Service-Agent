package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;

public class BusinessDetails {
    private String businessName;
    private String businessAddress;
    private List<String> telephoneNumber;
    private String emailAddress;
    private String website;
    private String businessDescription;
    private String businessCategory;
    private String businessOwner;
    private List<String> ownerTelephoneNumbers;
    private String businessHours;
    private List<String> socialLinks;

    /**
     * Example values used as a starting point when a new business is created.
     * These live in config.json (as "defaultBusinessDetails") so the owner can
     * change what a fresh business is pre-filled with, then edit the real values
     * later in the business's own business.json.
     */
    public static BusinessDetails placeholder() {
        BusinessDetails d = new BusinessDetails();
        d.businessName = "";  // filled in with the real name when the business is created
        d.businessAddress = "123 Example Road, Dhaka";
        d.telephoneNumber = new ArrayList<>(List.of("+8800000000000"));
        d.emailAddress = "business@example.com";
        d.website = "https://example.com";
        d.businessDescription = "Short description of what this business does.";
        d.businessCategory = "General";
        d.businessOwner = "Owner Name";
        d.ownerTelephoneNumbers = new ArrayList<>(List.of("+8800000000000"));
        d.businessHours = "Sat-Thu 10am-8pm, Friday closed";
        d.socialLinks = new ArrayList<>(List.of("https://facebook.com/example"));
        return d;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public List<String> getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(List<String> telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public List<String> getOwnerTelephoneNumbers() {
        return ownerTelephoneNumbers;
    }

    public void setOwnerTelephoneNumbers(List<String> ownerTelephoneNumbers) {
        this.ownerTelephoneNumbers = ownerTelephoneNumbers;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public List<String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<String> socialLinks) {
        this.socialLinks = socialLinks;
    }
}
