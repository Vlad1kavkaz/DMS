package ru.vlad.satellitedb.model;

import java.time.LocalDate;
import java.util.Arrays;

public class Satellite {

    private Integer id;
    private Integer satelliteSeriesId;
    private String name;
    private String code;
    private String internationalDesignator;
    private Integer noradCatalogNumber;
    private String purpose;
    private String status;
    private LocalDate launchDate;
    private LocalDate operationStartDate;
    private LocalDate decommissionDate;
    private Integer operatorOrganizationId;
    private Integer ownerOrganizationId;
    private Integer manufacturerOrganizationId;
    private String description;
    private String notes;
    private byte[] photo;

    public Satellite() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSatelliteSeriesId() {
        return satelliteSeriesId;
    }

    public void setSatelliteSeriesId(Integer satelliteSeriesId) {
        this.satelliteSeriesId = satelliteSeriesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInternationalDesignator() {
        return internationalDesignator;
    }

    public void setInternationalDesignator(String internationalDesignator) {
        this.internationalDesignator = internationalDesignator;
    }

    public Integer getNoradCatalogNumber() {
        return noradCatalogNumber;
    }

    public void setNoradCatalogNumber(Integer noradCatalogNumber) {
        this.noradCatalogNumber = noradCatalogNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }

    public LocalDate getOperationStartDate() {
        return operationStartDate;
    }

    public void setOperationStartDate(LocalDate operationStartDate) {
        this.operationStartDate = operationStartDate;
    }

    public LocalDate getDecommissionDate() {
        return decommissionDate;
    }

    public void setDecommissionDate(LocalDate decommissionDate) {
        this.decommissionDate = decommissionDate;
    }

    public Integer getOperatorOrganizationId() {
        return operatorOrganizationId;
    }

    public void setOperatorOrganizationId(Integer operatorOrganizationId) {
        this.operatorOrganizationId = operatorOrganizationId;
    }

    public Integer getOwnerOrganizationId() {
        return ownerOrganizationId;
    }

    public void setOwnerOrganizationId(Integer ownerOrganizationId) {
        this.ownerOrganizationId = ownerOrganizationId;
    }

    public Integer getManufacturerOrganizationId() {
        return manufacturerOrganizationId;
    }

    public void setManufacturerOrganizationId(Integer manufacturerOrganizationId) {
        this.manufacturerOrganizationId = manufacturerOrganizationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public byte[] getPhoto() {
        return photo != null ? Arrays.copyOf(photo, photo.length) : null;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo != null ? Arrays.copyOf(photo, photo.length) : null;
    }
}