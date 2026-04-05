package ru.vlad.satellitedb.model;

public class PolarOrbit extends Orbit {

    private Double meanAltitudeKm;
    private Double orbitalPeriodMin;
    private Boolean sunSynchronous;

    public Double getMeanAltitudeKm() {
        return meanAltitudeKm;
    }

    public void setMeanAltitudeKm(Double meanAltitudeKm) {
        this.meanAltitudeKm = meanAltitudeKm;
    }

    public Double getOrbitalPeriodMin() {
        return orbitalPeriodMin;
    }

    public void setOrbitalPeriodMin(Double orbitalPeriodMin) {
        this.orbitalPeriodMin = orbitalPeriodMin;
    }

    public Boolean getSunSynchronous() {
        return sunSynchronous;
    }

    public void setSunSynchronous(Boolean sunSynchronous) {
        this.sunSynchronous = sunSynchronous;
    }
}