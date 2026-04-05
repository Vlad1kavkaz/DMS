package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.model.GeoOrbit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GeoOrbitDao {

    public void insert(Connection connection, GeoOrbit orbit) throws SQLException {
        String sql = """
                INSERT INTO sc_cogs.tb_geo_orbit (
                    ni_id_orbit,
                    n_station_longitude_deg,
                    n_nominal_altitude_km,
                    n_orbital_period_min
                ) VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbit.getId());
            statement.setDouble(2, orbit.getStationLongitudeDeg());
            statement.setObject(3, orbit.getNominalAltitudeKm());
            statement.setObject(4, orbit.getOrbitalPeriodMin());
            statement.executeUpdate();
        }
    }

    public boolean update(Connection connection, GeoOrbit orbit) throws SQLException {
        String sql = """
                UPDATE sc_cogs.tb_geo_orbit
                SET
                    n_station_longitude_deg = ?,
                    n_nominal_altitude_km = ?,
                    n_orbital_period_min = ?
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, orbit.getStationLongitudeDeg());
            statement.setObject(2, orbit.getNominalAltitudeKm());
            statement.setObject(3, orbit.getOrbitalPeriodMin());
            statement.setInt(4, orbit.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public GeoOrbit findByOrbitId(Connection connection, int orbitId) throws SQLException {
        String sql = """
                SELECT
                    n_station_longitude_deg,
                    n_nominal_altitude_km,
                    n_orbital_period_min
                FROM sc_cogs.tb_geo_orbit
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbitId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                GeoOrbit orbit = new GeoOrbit();
                orbit.setStationLongitudeDeg(toDouble(rs.getObject("n_station_longitude_deg")));
                orbit.setNominalAltitudeKm(toDouble(rs.getObject("n_nominal_altitude_km")));
                orbit.setOrbitalPeriodMin(toDouble(rs.getObject("n_orbital_period_min")));
                return orbit;
            }
        }
    }

    private Double toDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }
}