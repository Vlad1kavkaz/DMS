package ru.vlad.satellitedb.model;

public class CrossSatellitePayload {

    private Integer id;
    private Integer satelliteId;
    private Integer payloadId;
    private Boolean isPrimary;
    private String notes;

    public CrossSatellitePayload() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSatelliteId() {
        return satelliteId;
    }

    public void setSatelliteId(Integer satelliteId) {
        this.satelliteId = satelliteId;
    }

    public Integer getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(Integer payloadId) {
        this.payloadId = payloadId;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}