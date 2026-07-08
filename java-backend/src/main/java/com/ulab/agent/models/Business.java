package com.ulab.agent.models;

public class Business {
    private String businessId;
    private String businessName;
    private BusinessDetails businessDetails;
    private String registerDate;

    public Business() {}

    public Business(String businessId, String businessName, BusinessDetails businessDetails, String registerDate) {
        this.businessId = businessId;
        this.businessName = businessName;
        this.businessDetails = businessDetails;
        this.registerDate = registerDate;
    }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public BusinessDetails getBusinessDetails() { return businessDetails; }
    public void setBusinessDetails(BusinessDetails businessDetails) { this.businessDetails = businessDetails; }

    public String getRegisterDate() { return registerDate; }
    public void setRegisterDate(String registerDate) { this.registerDate = registerDate; }
}
