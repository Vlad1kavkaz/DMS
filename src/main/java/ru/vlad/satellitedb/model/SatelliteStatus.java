package ru.vlad.satellitedb.model;

public class SatelliteStatus {

    private String status;
    private String description;

    public SatelliteStatus() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}