package ru.vlad.satellitedb.model;

public class Payload {

    private Integer id;
    private String code;
    private String name;
    private String type;
    private Integer manufacturerOrganizationId;
    private String description;

    public Payload() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}