package com.sapphire.userservice.model;

import java.util.UUID;

public class SubscriptionRequest {

    private UUID serviceId;
    private String endDate;

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

// Made with Bob
