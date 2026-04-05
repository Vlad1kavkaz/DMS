package ru.vlad.satellitedb.model;

public class HeoOrbit extends Orbit {

    private Double eccentricity;
    private Double perigeeAltitudeKm;
    private Double apogeeAltitudeKm;
    private Double orbitalPeriodMin;

    public Double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(Double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public Double getPerigeeAltitudeKm() {
        return perigeeAltitudeKm;
    }

    public void setPerigeeAltitudeKm(Double perigeeAltitudeKm) {
        this.perigeeAltitudeKm = perigeeAltitudeKm;
    }

    public Double getApogeeAltitudeKm() {
        return apogeeAltitudeKm;
    }

    public void setApogeeAltitudeKm(Double apogeeAltitudeKm) {
        this.apogeeAltitudeKm = apogeeAltitudeKm;
    }

    public Double getOrbitalPeriodMin() {
        return orbitalPeriodMin;
    }

    public void setOrbitalPeriodMin(Double orbitalPeriodMin) {
        this.orbitalPeriodMin = orbitalPeriodMin;
    }
}