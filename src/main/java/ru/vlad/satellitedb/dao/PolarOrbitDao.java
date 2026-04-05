package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.model.PolarOrbit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PolarOrbitDao {

    public void insert(Connection connection, PolarOrbit orbit) throws SQLException {
        String sql = """
                INSERT INTO sc_cogs.tb_polar_orbit (
                    ni_id_orbit,
                    n_mean_altitude_km,
                    n_orbital_period_min,
                    bl_is_sun_synchronous
                ) VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbit.getId());
            statement.setDouble(2, orbit.getMeanAltitudeKm());
            statement.setObject(3, orbit.getOrbitalPeriodMin());
            statement.setBoolean(4, orbit.getSunSynchronous() != null ? orbit.getSunSynchronous() : false);
            statement.executeUpdate();
        }
    }

    public boolean update(Connection connection, PolarOrbit orbit) throws SQLException {
        String sql = """
                UPDATE sc_cogs.tb_polar_orbit
                SET
                    n_mean_altitude_km = ?,
                    n_orbital_period_min = ?,
                    bl_is_sun_synchronous = ?
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, orbit.getMeanAltitudeKm());
            statement.setObject(2, orbit.getOrbitalPeriodMin());
            statement.setBoolean(3, orbit.getSunSynchronous() != null ? orbit.getSunSynchronous() : false);
            statement.setInt(4, orbit.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public PolarOrbit findByOrbitId(Connection connection, int orbitId) throws SQLException {
        String sql = """
                SELECT
                    n_mean_altitude_km,
                    n_orbital_period_min,
                    bl_is_sun_synchronous
                FROM sc_cogs.tb_polar_orbit
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbitId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                PolarOrbit orbit = new PolarOrbit();
                orbit.setMeanAltitudeKm(toDouble(rs.getObject("n_mean_altitude_km")));
                orbit.setOrbitalPeriodMin(toDouble(rs.getObject("n_orbital_period_min")));
                orbit.setSunSynchronous(rs.getBoolean("bl_is_sun_synchronous"));
                return orbit;
            }
        }
    }

    private Double toDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }
}