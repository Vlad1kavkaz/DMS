package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.SatelliteStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SatelliteStatusDao {

    public SatelliteStatus insert(SatelliteStatus status) {
        String sql = """
                INSERT INTO sc_cogs.tb_satellite_status (
                    cv_satellite_status,
                    ct_description
                ) VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, status);
            statement.executeUpdate();

            return status;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении статуса спутника", e);
        }
    }

    public boolean update(SatelliteStatus status, String oldStatus) {
        String sql = """
                UPDATE sc_cogs.tb_satellite_status
                SET
                    cv_satellite_status = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE cv_satellite_status = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, status);
            statement.setString(3, oldStatus);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении статуса спутника status=" + oldStatus, e);
        }
    }

    public boolean deleteByStatus(String status) {
        String sql = "DELETE FROM sc_cogs.tb_satellite_status WHERE cv_satellite_status = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении статуса спутника status=" + status, e);
        }
    }

    public List<SatelliteStatus> findAll() {
        String sql = """
                SELECT
                    cv_satellite_status,
                    ct_description
                FROM sc_cogs.tb_satellite_status
                ORDER BY cv_satellite_status
                """;

        List<SatelliteStatus> statuses = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                statuses.add(mapRow(rs));
            }

            return statuses;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении статусов спутников", e);
        }
    }

    private SatelliteStatus mapRow(ResultSet rs) throws SQLException {
        SatelliteStatus status = new SatelliteStatus();
        status.setStatus(rs.getString("cv_satellite_status"));
        status.setDescription(rs.getString("ct_description"));
        return status;
    }

    private void fillStatement(PreparedStatement statement, SatelliteStatus status) throws SQLException {
        statement.setString(1, status.getStatus());
        statement.setString(2, status.getDescription());
    }
}