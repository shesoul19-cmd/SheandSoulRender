package com.sheandsoul.v1update.dto;

public class ProfileServiceDto {

    private boolean enableMenstrualService;
    private boolean enableBreastCancerService;

    

    public boolean isMenstrualServiceEnabled() {
        return enableMenstrualService;
    
    }
    public void setMenstrualServiceEnabled(boolean enableMenstrualService) {
        this.enableMenstrualService = enableMenstrualService;
    }
    
    public boolean isBreastCancerServiceEnabled() {
        return enableBreastCancerService;
    }
    
    public void setBreastCancerServiceEnabled(boolean enableBreastCancerService) {
        this.enableBreastCancerService = enableBreastCancerService;
    }
}
