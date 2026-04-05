package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.SatelliteDao;
import ru.vlad.satellitedb.model.Satellite;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SatelliteService {

    private static final Set<String> ALLOWED_PURPOSES = Set.of(
            "meteorology",
            "hydrology",
            "remote_sensing",
            "climate_monitoring",
            "ocean_monitoring",
            "ice_monitoring",
            "environment_monitoring",
            "multi_purpose",
            "other"
    );

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "planned",
            "active",
            "reserve",
            "inactive",
            "lost",
            "retired"
    );

    private final SatelliteDao satelliteDao = new SatelliteDao();

    public List<Satellite> getAllSatellites() {
        return satelliteDao.findAll();
    }

    public Optional<Satellite> getSatelliteById(int id) {
        return satelliteDao.findById(id);
    }

    public void createSatellite(Satellite satellite) {
        validateSatellite(satellite, false);
        satelliteDao.insert(satellite);
    }

    public void updateSatellite(Satellite satellite) {
        validateSatellite(satellite, true);
        satelliteDao.update(satellite);
    }

    public void deleteSatellite(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id спутника");
        }
        satelliteDao.deleteById(id);
    }

    private void validateSatellite(Satellite satellite, boolean idRequired) {
        if (satellite == null) {
            throw new IllegalArgumentException("Спутник не должен быть null");
        }

        if (idRequired && (satellite.getId() == null || satellite.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id");
        }

        if (satellite.getName() == null || satellite.getName().isBlank()) {
            throw new IllegalArgumentException("Название спутника обязательно");
        }

        if (satellite.getPurpose() == null || !ALLOWED_PURPOSES.contains(satellite.getPurpose())) {
            throw new IllegalArgumentException("Некорректное назначение спутника");
        }

        if (satellite.getStatus() == null || !ALLOWED_STATUSES.contains(satellite.getStatus())) {
            throw new IllegalArgumentException("Некорректный статус спутника");
        }

        if (satellite.getDecommissionDate() != null
                && satellite.getLaunchDate() != null
                && satellite.getDecommissionDate().isBefore(satellite.getLaunchDate())) {
            throw new IllegalArgumentException("Дата списания не может быть раньше даты запуска");
        }
    }
}