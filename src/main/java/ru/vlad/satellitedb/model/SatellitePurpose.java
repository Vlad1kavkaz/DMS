package ru.vlad.satellitedb.model;

public class SatellitePurpose {

    private String purpose;
    private String description;

    public SatellitePurpose() {
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}