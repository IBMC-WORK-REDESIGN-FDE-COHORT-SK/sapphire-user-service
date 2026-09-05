package com.sapphire.userservice.model;

import java.util.Map;

public class UpdateUserRequest {

    private String fullName;
    private String tier;
    private Map<String, Object> address;
    private Map<String, Object> physicalAttributes;

    public String getFullName() {
        return fullName;
    }

    public String getTier() {
        return tier;
    }

    public Map<String, Object> getAddress() {
        return address;
    }

    public Map<String, Object> getPhysicalAttributes() {
        return physicalAttributes;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }

    public void setPhysicalAttributes(Map<String, Object> physicalAttributes) {
        this.physicalAttributes = physicalAttributes;
    }
}