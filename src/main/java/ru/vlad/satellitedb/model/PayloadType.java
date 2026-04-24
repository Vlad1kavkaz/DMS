package ru.vlad.satellitedb.model;

public class PayloadType {

    private String type;
    private String description;

    public PayloadType() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}