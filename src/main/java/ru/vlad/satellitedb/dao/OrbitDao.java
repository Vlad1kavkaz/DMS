package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.model.Orbit;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrbitDao {

    public Integer insert(Connection connection, Orbit orbit) throws SQLException {
        String sql = """
                INSERT INTO sc_cogs.tb_orbit (
                    ni_id_satellite,
                    cv_orbit_type,
                    n_inclination_deg,
                    dt_valid_from,
                    dt_valid_to,
                    bl_is_current,
                    ct_notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING ni_id_orbit
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbit.getSatelliteId());
            statement.setString(2, orbit.getOrbitType());
            statement.setObject(3, orbit.getInclinationDeg());
            statement.setDate(4, toSqlDate(orbit.getValidFrom()));
            statement.setDate(5, toSqlDate(orbit.getValidTo()));
            statement.setBoolean(6, orbit.getCurrent() != null ? orbit.getCurrent() : false);
            statement.setString(7, orbit.getNotes());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ni_id_orbit");
                }
                throw new SQLException("Не удалось получить id созданной орбиты");
            }
        }
    }

    public boolean updateBase(Connection connection, Orbit orbit) throws SQLException {
        String sql = """
                UPDATE sc_cogs.tb_orbit
                SET
                    ni_id_satellite = ?,
                    cv_orbit_type = ?,
                    n_inclination_deg = ?,
                    dt_valid_from = ?,
                    dt_valid_to = ?,
                    bl_is_current = ?,
                    ct_notes = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbit.getSatelliteId());
            statement.setString(2, orbit.getOrbitType());
            statement.setObject(3, orbit.getInclinationDeg());
            statement.setDate(4, toSqlDate(orbit.getValidFrom()));
            statement.setDate(5, toSqlDate(orbit.getValidTo()));
            statement.setBoolean(6, orbit.getCurrent() != null ? orbit.getCurrent() : false);
            statement.setString(7, orbit.getNotes());
            statement.setInt(8, orbit.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public Orbit findBaseById(Connection connection, int orbitId) throws SQLException {
        String sql = """
                SELECT
                    ni_id_orbit,
                    ni_id_satellite,
                    cv_orbit_type,
                    n_inclination_deg,
                    dt_valid_from,
                    dt_valid_to,
                    bl_is_current,
                    ct_notes
                FROM sc_cogs.tb_orbit
                WHERE ni_id_orbit = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbitId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Orbit orbit = new Orbit();
                orbit.setId(rs.getInt("ni_id_orbit"));
                orbit.setSatelliteId(rs.getInt("ni_id_satellite"));
                orbit.setOrbitType(rs.getString("cv_orbit_type"));
                orbit.setInclinationDeg(toDouble(rs.getObject("n_inclination_deg")));                orbit.setValidFrom(toLocalDate(rs.getDate("dt_valid_from")));
                orbit.setValidFrom(toLocalDate(rs.getDate("dt_valid_from")));
                orbit.setValidTo(toLocalDate(rs.getDate("dt_valid_to")));
                orbit.setCurrent(rs.getBoolean("bl_is_current"));
                orbit.setNotes(rs.getString("ct_notes"));
                return orbit;
            }
        }
    }

    public List<Orbit> findAllBase(Connection connection) throws SQLException {
        String sql = """
                SELECT
                    ni_id_orbit,
                    ni_id_satellite,
                    cv_orbit_type,
                    n_inclination_deg,
                    dt_valid_from,
                    dt_valid_to,
                    bl_is_current,
                    ct_notes
                FROM sc_cogs.tb_orbit
                ORDER BY ni_id_orbit
                """;

        List<Orbit> orbits = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Orbit orbit = new Orbit();
                orbit.setId(rs.getInt("ni_id_orbit"));
                orbit.setSatelliteId(rs.getInt("ni_id_satellite"));
                orbit.setOrbitType(rs.getString("cv_orbit_type"));
                orbit.setInclinationDeg(toDouble(rs.getObject("n_inclination_deg")));                orbit.setValidFrom(toLocalDate(rs.getDate("dt_valid_from")));
                orbit.setValidTo(toLocalDate(rs.getDate("dt_valid_to")));
                orbit.setCurrent(rs.getBoolean("bl_is_current"));
                orbit.setNotes(rs.getString("ct_notes"));
                orbits.add(orbit);
            }
        }

        return orbits;
    }

    public boolean deleteById(Connection connection, int orbitId) throws SQLException {
        String sql = "DELETE FROM sc_cogs.tb_orbit WHERE ni_id_orbit = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orbitId);
            return statement.executeUpdate() > 0;
        }
    }

    private Date toSqlDate(java.time.LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private Double toDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }
}