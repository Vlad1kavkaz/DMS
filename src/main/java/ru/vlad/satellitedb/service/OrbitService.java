package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.GeoOrbitDao;
import ru.vlad.satellitedb.dao.HeoOrbitDao;
import ru.vlad.satellitedb.dao.OrbitDao;
import ru.vlad.satellitedb.dao.PolarOrbitDao;
import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.GeoOrbit;
import ru.vlad.satellitedb.model.HeoOrbit;
import ru.vlad.satellitedb.model.Orbit;
import ru.vlad.satellitedb.model.PolarOrbit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class OrbitService {

    private static final Set<String> ALLOWED_ORBIT_TYPES = Set.of(
            "geostationary",
            "highly_elliptical",
            "polar"
    );

    private final OrbitDao orbitDao = new OrbitDao();
    private final GeoOrbitDao geoOrbitDao = new GeoOrbitDao();
    private final HeoOrbitDao heoOrbitDao = new HeoOrbitDao();
    private final PolarOrbitDao polarOrbitDao = new PolarOrbitDao();

    public void createGeoOrbit(GeoOrbit orbit) {
        validateBaseOrbit(orbit, "geostationary", false);
        validateGeoOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer orbitId = orbitDao.insert(connection, orbit);
                orbit.setId(orbitId);
                geoOrbitDao.insert(connection, orbit);
                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при создании GEO-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при создании GEO-орбиты", e);
        }
    }

    public void createHeoOrbit(HeoOrbit orbit) {
        validateBaseOrbit(orbit, "highly_elliptical", false);
        validateHeoOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer orbitId = orbitDao.insert(connection, orbit);
                orbit.setId(orbitId);
                heoOrbitDao.insert(connection, orbit);
                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при создании HEO-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при создании HEO-орбиты", e);
        }
    }

    public void createPolarOrbit(PolarOrbit orbit) {
        validateBaseOrbit(orbit, "polar", false);
        validatePolarOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer orbitId = orbitDao.insert(connection, orbit);
                orbit.setId(orbitId);
                polarOrbitDao.insert(connection, orbit);
                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при создании POLAR-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при создании POLAR-орбиты", e);
        }
    }

    public GeoOrbit getGeoOrbitById(int orbitId) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Orbit baseOrbit = orbitDao.findBaseById(connection, orbitId);
            if (baseOrbit == null) {
                return null;
            }
            if (!"geostationary".equals(baseOrbit.getOrbitType())) {
                throw new IllegalArgumentException("Орбита id=" + orbitId + " не является GEO");
            }

            GeoOrbit specific = geoOrbitDao.findByOrbitId(connection, orbitId);
            if (specific == null) {
                return null;
            }

            copyBaseToGeo(baseOrbit, specific);
            return specific;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении GEO-орбиты id=" + orbitId, e);
        }
    }

    public HeoOrbit getHeoOrbitById(int orbitId) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Orbit baseOrbit = orbitDao.findBaseById(connection, orbitId);
            if (baseOrbit == null) {
                return null;
            }
            if (!"highly_elliptical".equals(baseOrbit.getOrbitType())) {
                throw new IllegalArgumentException("Орбита id=" + orbitId + " не является HEO");
            }

            HeoOrbit specific = heoOrbitDao.findByOrbitId(connection, orbitId);
            if (specific == null) {
                return null;
            }

            copyBaseToHeo(baseOrbit, specific);
            return specific;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении HEO-орбиты id=" + orbitId, e);
        }
    }

    public PolarOrbit getPolarOrbitById(int orbitId) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Orbit baseOrbit = orbitDao.findBaseById(connection, orbitId);
            if (baseOrbit == null) {
                return null;
            }
            if (!"polar".equals(baseOrbit.getOrbitType())) {
                throw new IllegalArgumentException("Орбита id=" + orbitId + " не является POLAR");
            }

            PolarOrbit specific = polarOrbitDao.findByOrbitId(connection, orbitId);
            if (specific == null) {
                return null;
            }

            copyBaseToPolar(baseOrbit, specific);
            return specific;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении POLAR-орбиты id=" + orbitId, e);
        }
    }

    public List<Orbit> getAllOrbits() {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return orbitDao.findAllBase(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка орбит", e);
        }
    }

    public void updateGeoOrbit(GeoOrbit orbit) {
        validateBaseOrbit(orbit, "geostationary", true);
        validateGeoOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean updatedBase = orbitDao.updateBase(connection, orbit);
                boolean updatedSpecific = geoOrbitDao.update(connection, orbit);

                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при обновлении GEO-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при обновлении GEO-орбиты", e);
        }
    }

    public void updateHeoOrbit(HeoOrbit orbit) {
        validateBaseOrbit(orbit, "highly_elliptical", true);
        validateHeoOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean updatedBase = orbitDao.updateBase(connection, orbit);
                boolean updatedSpecific = heoOrbitDao.update(connection, orbit);

                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при обновлении HEO-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при обновлении HEO-орбиты", e);
        }
    }

    public void updatePolarOrbit(PolarOrbit orbit) {
        validateBaseOrbit(orbit, "polar", true);
        validatePolarOrbit(orbit);

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean updatedBase = orbitDao.updateBase(connection, orbit);
                boolean updatedSpecific = polarOrbitDao.update(connection, orbit);

                connection.commit();
            } catch (Exception e) {
                rollbackQuietly(connection);
                throw new RuntimeException("Ошибка при обновлении POLAR-орбиты", e);
            } finally {
                restoreAutoCommit(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка соединения при обновлении POLAR-орбиты", e);
        }
    }

    public void deleteOrbit(int orbitId) {
        if (orbitId <= 0) {
            throw new IllegalArgumentException("Некорректный id орбиты");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            orbitDao.deleteById(connection, orbitId);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении орбиты id=" + orbitId, e);
        }
    }

    private void validateBaseOrbit(Orbit orbit, String expectedType, boolean idRequired) {
        if (orbit == null) {
            throw new IllegalArgumentException("Орбита не должна быть null");
        }

        if (idRequired && (orbit.getId() == null || orbit.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id орбиты");
        }

        if (orbit.getSatelliteId() == null || orbit.getSatelliteId() <= 0) {
            throw new IllegalArgumentException("Некорректный satelliteId");
        }

        if (orbit.getOrbitType() == null || !ALLOWED_ORBIT_TYPES.contains(orbit.getOrbitType())) {
            throw new IllegalArgumentException("Некорректный тип орбиты");
        }

        if (!expectedType.equals(orbit.getOrbitType())) {
            throw new IllegalArgumentException("Тип орбиты не соответствует вызываемому методу");
        }

        if (orbit.getInclinationDeg() != null
                && (orbit.getInclinationDeg() < 0 || orbit.getInclinationDeg() > 180)) {
            throw new IllegalArgumentException("Наклонение должно быть в диапазоне [0; 180]");
        }

        if (orbit.getValidFrom() != null
                && orbit.getValidTo() != null
                && orbit.getValidTo().isBefore(orbit.getValidFrom())) {
            throw new IllegalArgumentException("Дата окончания действия не может быть раньше даты начала");
        }
    }

    private void validateGeoOrbit(GeoOrbit orbit) {
        if (orbit.getStationLongitudeDeg() == null) {
            throw new IllegalArgumentException("Для GEO-орбиты обязательна долгота стояния");
        }

        if (orbit.getStationLongitudeDeg() < -180 || orbit.getStationLongitudeDeg() > 180) {
            throw new IllegalArgumentException("Долгота стояния должна быть в диапазоне [-180; 180]");
        }

        if (orbit.getNominalAltitudeKm() != null && orbit.getNominalAltitudeKm() < 0) {
            throw new IllegalArgumentException("Номинальная высота не может быть отрицательной");
        }

        if (orbit.getOrbitalPeriodMin() != null && orbit.getOrbitalPeriodMin() <= 0) {
            throw new IllegalArgumentException("Орбитальный период должен быть положительным");
        }
    }

    private void validateHeoOrbit(HeoOrbit orbit) {
        if (orbit.getEccentricity() == null) {
            throw new IllegalArgumentException("Для HEO-орбиты обязателен эксцентриситет");
        }

        if (orbit.getEccentricity() < 0 || orbit.getEccentricity() >= 1) {
            throw new IllegalArgumentException("Эксцентриситет должен быть в диапазоне [0; 1)");
        }

        if (orbit.getPerigeeAltitudeKm() == null || orbit.getPerigeeAltitudeKm() < 0) {
            throw new IllegalArgumentException("Перигей должен быть >= 0");
        }

        if (orbit.getApogeeAltitudeKm() == null || orbit.getApogeeAltitudeKm() < orbit.getPerigeeAltitudeKm()) {
            throw new IllegalArgumentException("Апогей должен быть >= перигея");
        }

        if (orbit.getOrbitalPeriodMin() != null && orbit.getOrbitalPeriodMin() <= 0) {
            throw new IllegalArgumentException("Орбитальный период должен быть положительным");
        }
    }

    private void validatePolarOrbit(PolarOrbit orbit) {
        if (orbit.getMeanAltitudeKm() == null || orbit.getMeanAltitudeKm() < 0) {
            throw new IllegalArgumentException("Для POLAR-орбиты средняя высота обязательна и должна быть >= 0");
        }

        if (orbit.getOrbitalPeriodMin() != null && orbit.getOrbitalPeriodMin() <= 0) {
            throw new IllegalArgumentException("Орбитальный период должен быть положительным");
        }
    }

    private void copyBaseToGeo(Orbit base, GeoOrbit target) {
        target.setId(base.getId());
        target.setSatelliteId(base.getSatelliteId());
        target.setOrbitType(base.getOrbitType());
        target.setInclinationDeg(base.getInclinationDeg());
        target.setValidFrom(base.getValidFrom());
        target.setValidTo(base.getValidTo());
        target.setCurrent(base.getCurrent());
        target.setNotes(base.getNotes());
    }

    private void copyBaseToHeo(Orbit base, HeoOrbit target) {
        target.setId(base.getId());
        target.setSatelliteId(base.getSatelliteId());
        target.setOrbitType(base.getOrbitType());
        target.setInclinationDeg(base.getInclinationDeg());
        target.setValidFrom(base.getValidFrom());
        target.setValidTo(base.getValidTo());
        target.setCurrent(base.getCurrent());
        target.setNotes(base.getNotes());
    }

    private void copyBaseToPolar(Orbit base, PolarOrbit target) {
        target.setId(base.getId());
        target.setSatelliteId(base.getSatelliteId());
        target.setOrbitType(base.getOrbitType());
        target.setInclinationDeg(base.getInclinationDeg());
        target.setValidFrom(base.getValidFrom());
        target.setValidTo(base.getValidTo());
        target.setCurrent(base.getCurrent());
        target.setNotes(base.getNotes());
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}