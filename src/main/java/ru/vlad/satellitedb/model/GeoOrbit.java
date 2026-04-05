package ru.vlad.satellitedb.model;

public class GeoOrbit extends Orbit {

    private Double stationLongitudeDeg;
    private Double nominalAltitudeKm;
    private Double orbitalPeriodMin;

    public Double getStationLongitudeDeg() {
        return stationLongitudeDeg;
    }

    public void setStationLongitudeDeg(Double stationLongitudeDeg) {
        this.stationLongitudeDeg = stationLongitudeDeg;
    }

    public Double getNominalAltitudeKm() {
        return nominalAltitudeKm;
    }

    public void setNominalAltitudeKm(Double nominalAltitudeKm) {
        this.nominalAltitudeKm = nominalAltitudeKm;
    }

    public Double getOrbitalPeriodMin() {
        return orbitalPeriodMin;
    }

    public void setOrbitalPeriodMin(Double orbitalPeriodMin) {
        this.orbitalPeriodMin = orbitalPeriodMin;
    }
}