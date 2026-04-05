package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.CrossSatellitePayload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrossSatellitePayloadDao {

    public CrossSatellitePayload insert(CrossSatellitePayload link) {
        String sql = """
                INSERT INTO sc_cogs.tb_cross_satellite_payload (
                    ni_id_satellite,
                    ni_id_payload,
                    bl_is_primary,
                    ct_notes
                ) VALUES (?, ?, ?, ?)
                RETURNING ni_id_cross_satellite_payload
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, link);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    link.setId(rs.getInt("ni_id_cross_satellite_payload"));
                }
            }

            return link;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении связи спутник-полезная нагрузка", e);
        }
    }

    public boolean update(CrossSatellitePayload link) {
        String sql = """
                UPDATE sc_cogs.tb_cross_satellite_payload
                SET
                    ni_id_satellite = ?,
                    ni_id_payload = ?,
                    bl_is_primary = ?,
                    ct_notes = ?
                WHERE ni_id_cross_satellite_payload = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, link);
            statement.setInt(5, link.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении связи id=" + link.getId(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM sc_cogs.tb_cross_satellite_payload WHERE ni_id_cross_satellite_payload = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении связи id=" + id, e);
        }
    }

    public List<CrossSatellitePayload> findBySatelliteId(int satelliteId) {
        String sql = """
            SELECT
                ni_id_cross_satellite_payload,
                ni_id_satellite,
                ni_id_payload,
                bl_is_primary,
                ct_notes
            FROM sc_cogs.tb_cross_satellite_payload
            WHERE ni_id_satellite = ?
            ORDER BY ni_id_cross_satellite_payload
            """;

        List<CrossSatellitePayload> links = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, satelliteId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    links.add(mapRow(rs));
                }
            }

            return links;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении связей по satelliteId=" + satelliteId, e);
        }
    }

    private CrossSatellitePayload mapRow(ResultSet rs) throws SQLException {
        CrossSatellitePayload link = new CrossSatellitePayload();
        link.setId(rs.getInt("ni_id_cross_satellite_payload"));
        link.setSatelliteId(rs.getInt("ni_id_satellite"));
        link.setPayloadId(rs.getInt("ni_id_payload"));
        link.setIsPrimary(rs.getBoolean("bl_is_primary"));
        link.setNotes(rs.getString("ct_notes"));
        return link;
    }

    private void fillStatement(PreparedStatement statement, CrossSatellitePayload link) throws SQLException {
        statement.setInt(1, link.getSatelliteId());
        statement.setInt(2, link.getPayloadId());
        statement.setBoolean(3, link.getIsPrimary() != null ? link.getIsPrimary() : false);
        statement.setString(4, link.getNotes());
    }
}