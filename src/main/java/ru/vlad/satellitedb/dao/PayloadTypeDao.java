package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.PayloadType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PayloadTypeDao {

    public PayloadType insert(PayloadType type) {
        String sql = """
                INSERT INTO sc_cogs.tb_payload_type (
                    cv_payload_type,
                    ct_description
                ) VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, type);
            statement.executeUpdate();

            return type;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении типа полезной нагрузки", e);
        }
    }

    public boolean update(PayloadType type, String oldType) {
        String sql = """
                UPDATE sc_cogs.tb_payload_type
                SET
                    cv_payload_type = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE cv_payload_type = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, type);
            statement.setString(3, oldType);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении типа полезной нагрузки type=" + oldType, e);
        }
    }

    public boolean deleteByType(String type) {
        String sql = "DELETE FROM sc_cogs.tb_payload_type WHERE cv_payload_type = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении типа полезной нагрузки type=" + type, e);
        }
    }

    public List<PayloadType> findAll() {
        String sql = """
                SELECT
                    cv_payload_type,
                    ct_description
                FROM sc_cogs.tb_payload_type
                ORDER BY cv_payload_type
                """;

        List<PayloadType> types = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                types.add(mapRow(rs));
            }

            return types;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении типов полезной нагрузки", e);
        }
    }

    private PayloadType mapRow(ResultSet rs) throws SQLException {
        PayloadType type = new PayloadType();
        type.setType(rs.getString("cv_payload_type"));
        type.setDescription(rs.getString("ct_description"));
        return type;
    }

    private void fillStatement(PreparedStatement statement, PayloadType type) throws SQLException {
        statement.setString(1, type.getType());
        statement.setString(2, type.getDescription());
    }
}