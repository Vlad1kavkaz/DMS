package ru.vlad.satellitedb.ui;

import ru.vlad.satellitedb.model.Payload;
import ru.vlad.satellitedb.model.Satellite;

import java.util.List;

public class SatelliteDetailsDto {

    private final Satellite satellite;
    private final String seriesName;
    private final String operatorName;
    private final String ownerName;
    private final String manufacturerName;
    private final String orbitType;
    private final List<Payload> payloads;

    public SatelliteDetailsDto(
            Satellite satellite,
            String seriesName,
            String operatorName,
            String ownerName,
            String manufacturerName,
            String orbitType,
            List<Payload> payloads
    ) {
        this.satellite = satellite;
        this.seriesName = seriesName;
        this.operatorName = operatorName;
        this.ownerName = ownerName;
        this.manufacturerName = manufacturerName;
        this.orbitType = orbitType;
        this.payloads = payloads;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public String getOrbitType() {
        return orbitType;
    }

    public List<Payload> getPayloads() {
        return payloads;
    }
}