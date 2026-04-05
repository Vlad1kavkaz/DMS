package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.model.GeoOrbit;
import ru.vlad.satellitedb.model.HeoOrbit;
import ru.vlad.satellitedb.model.Orbit;
import ru.vlad.satellitedb.model.PolarOrbit;
import ru.vlad.satellitedb.model.Satellite;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OrbitUiService {

    private final OrbitService orbitService = new OrbitService();
    private final SatelliteService satelliteService = new SatelliteService();

    public List<Satellite> getSatellitesWithoutOrbit() {
        List<Satellite> satellites = satelliteService.getAllSatellites();
        Set<Integer> satelliteIdsWithOrbit = orbitService.getAllOrbits().stream()
                .map(Orbit::getSatelliteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return satellites.stream()
                .filter(s -> s.getId() != null)
                .filter(s -> !satelliteIdsWithOrbit.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public Orbit getFullOrbitById(int orbitId) {
        Orbit baseOrbit = orbitService.getAllOrbits().stream()
                .filter(o -> o.getId() != null && o.getId() == orbitId)
                .findFirst()
                .orElse(null);

        if (baseOrbit == null || baseOrbit.getOrbitType() == null) {
            return null;
        }

        return switch (baseOrbit.getOrbitType()) {
            case "geostationary" -> orbitService.getGeoOrbitById(orbitId);
            case "highly_elliptical" -> orbitService.getHeoOrbitById(orbitId);
            case "polar" -> orbitService.getPolarOrbitById(orbitId);
            default -> null;
        };
    }

    public void createOrbit(Orbit orbit) {
        if (orbit instanceof GeoOrbit geoOrbit) {
            orbitService.createGeoOrbit(geoOrbit);
            return;
        }
        if (orbit instanceof HeoOrbit heoOrbit) {
            orbitService.createHeoOrbit(heoOrbit);
            return;
        }
        if (orbit instanceof PolarOrbit polarOrbit) {
            orbitService.createPolarOrbit(polarOrbit);
            return;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип орбиты для создания");
    }

    public void updateOrbit(Orbit orbit) {
        if (orbit instanceof GeoOrbit geoOrbit) {
            orbitService.updateGeoOrbit(geoOrbit);
            return;
        }
        if (orbit instanceof HeoOrbit heoOrbit) {
            orbitService.updateHeoOrbit(heoOrbit);
            return;
        }
        if (orbit instanceof PolarOrbit polarOrbit) {
            orbitService.updatePolarOrbit(polarOrbit);
            return;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип орбиты для обновления");
    }

    public void deleteOrbit(int orbitId) {
        orbitService.deleteOrbit(orbitId);
    }
}