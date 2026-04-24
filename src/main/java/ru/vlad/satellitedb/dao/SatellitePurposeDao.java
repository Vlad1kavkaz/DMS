package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.SatellitePurpose;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SatellitePurposeDao {

    public SatellitePurpose insert(SatellitePurpose purpose) {
        String sql = """
                INSERT INTO sc_cogs.tb_satellite_purpose (
                    cv_satellite_purpose,
                    ct_description
                ) VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, purpose);
            statement.executeUpdate();

            return purpose;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении назначения спутника", e);
        }
    }

    public boolean update(SatellitePurpose purpose, String oldPurpose) {
        String sql = """
                UPDATE sc_cogs.tb_satellite_purpose
                SET
                    cv_satellite_purpose = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE cv_satellite_purpose = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, purpose);
            statement.setString(3, oldPurpose);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении назначения спутника purpose=" + oldPurpose, e);
        }
    }

    public boolean deleteByPurpose(String purpose) {
        String sql = "DELETE FROM sc_cogs.tb_satellite_purpose WHERE cv_satellite_purpose = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, purpose);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении назначения спутника purpose=" + purpose, e);
        }
    }

    public List<SatellitePurpose> findAll() {
        String sql = """
                SELECT
                    cv_satellite_purpose,
                    ct_description
                FROM sc_cogs.tb_satellite_purpose
                ORDER BY cv_satellite_purpose
                """;

        List<SatellitePurpose> purposes = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                purposes.add(mapRow(rs));
            }

            return purposes;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении назначений спутников", e);
        }
    }

    private SatellitePurpose mapRow(ResultSet rs) throws SQLException {
        SatellitePurpose purpose = new SatellitePurpose();
        purpose.setPurpose(rs.getString("cv_satellite_purpose"));
        purpose.setDescription(rs.getString("ct_description"));
        return purpose;
    }

    private void fillStatement(PreparedStatement statement, SatellitePurpose purpose) throws SQLException {
        statement.setString(1, purpose.getPurpose());
        statement.setString(2, purpose.getDescription());
    }
}