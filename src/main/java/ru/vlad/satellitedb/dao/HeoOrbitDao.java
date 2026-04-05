package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.model.HeoOrbit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HeoOrbitDao {

    public void insert(Connection connection, HeoOrbit orbit) throws SQLException {
        String sql = """
                INSERT INTO sc_cogs.tb_heo_orbit (
                    ni_id_orbit,
                    n_eccentricity,
                    n_perigee_altitude_km,
                    n_apogee_altitude_km,
                    n_orbital_period_min
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbit.getId());
            statement.setDouble(2, orbit.getEccentricity());
            statement.setDouble(3, orbit.getPerigeeAltitudeKm());
            statement.setDouble(4, orbit.getApogeeAltitudeKm());
            statement.setObject(5, orbit.getOrbitalPeriodMin());
            statement.executeUpdate();
        }
    }

    public boolean update(Connection connection, HeoOrbit orbit) throws SQLException {
        String sql = """
                UPDATE sc_cogs.tb_heo_orbit
                SET
                    n_eccentricity = ?,
                    n_perigee_altitude_km = ?,
                    n_apogee_altitude_km = ?,
                    n_orbital_period_min = ?
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, orbit.getEccentricity());
            statement.setDouble(2, orbit.getPerigeeAltitudeKm());
            statement.setDouble(3, orbit.getApogeeAltitudeKm());
            statement.setObject(4, orbit.getOrbitalPeriodMin());
            statement.setInt(5, orbit.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public HeoOrbit findByOrbitId(Connection connection, int orbitId) throws SQLException {
        String sql = """
                SELECT
                    n_eccentricity,
                    n_perigee_altitude_km,
                    n_apogee_altitude_km,
                    n_orbital_period_min
                FROM sc_cogs.tb_heo_orbit
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbitId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                HeoOrbit orbit = new HeoOrbit();
                orbit.setEccentricity(toDouble(rs.getObject("n_eccentricity")));
                orbit.setPerigeeAltitudeKm(toDouble(rs.getObject("n_perigee_altitude_km")));
                orbit.setApogeeAltitudeKm(toDouble(rs.getObject("n_apogee_altitude_km")));
                orbit.setOrbitalPeriodMin(toDouble(rs.getObject("n_orbital_period_min")));
                return orbit;
            }
        }
    }

    private Double toDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }
}