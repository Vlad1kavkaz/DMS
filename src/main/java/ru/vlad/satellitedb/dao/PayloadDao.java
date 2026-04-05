package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.Payload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PayloadDao {

    public List<Payload> findAll() {
        String sql = """
                SELECT
                    ni_id_payload,
                    cv_payload_code,
                    cv_payload_name,
                    cv_payload_type,
                    ni_id_manufacturer_organization,
                    ct_description
                FROM sc_cogs.tb_payload
                ORDER BY ni_id_payload
                """;

        List<Payload> payloads = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                payloads.add(mapRow(rs));
            }

            return payloads;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка полезных нагрузок", e);
        }
    }

    public Optional<Payload> findById(int id) {
        String sql = """
                SELECT
                    ni_id_payload,
                    cv_payload_code,
                    cv_payload_name,
                    cv_payload_type,
                    ni_id_manufacturer_organization,
                    ct_description
                FROM sc_cogs.tb_payload
                WHERE ni_id_payload = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении полезной нагрузки по id=" + id, e);
        }
    }

    public Payload insert(Payload payload) {
        String sql = """
                INSERT INTO sc_cogs.tb_payload (
                    cv_payload_code,
                    cv_payload_name,
                    cv_payload_type,
                    ni_id_manufacturer_organization,
                    ct_description
                ) VALUES (?, ?, ?, ?, ?)
                RETURNING ni_id_payload
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, payload);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    payload.setId(rs.getInt("ni_id_payload"));
                }
            }

            return payload;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении полезной нагрузки", e);
        }
    }

    public boolean update(Payload payload) {
        String sql = """
                UPDATE sc_cogs.tb_payload
                SET
                    cv_payload_code = ?,
                    cv_payload_name = ?,
                    cv_payload_type = ?,
                    ni_id_manufacturer_organization = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE ni_id_payload = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, payload);
            statement.setInt(6, payload.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении полезной нагрузки id=" + payload.getId(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM sc_cogs.tb_payload WHERE ni_id_payload = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении полезной нагрузки id=" + id, e);
        }
    }

    private Payload mapRow(ResultSet rs) throws SQLException {
        Payload payload = new Payload();
        payload.setId(rs.getInt("ni_id_payload"));
        payload.setCode(rs.getString("cv_payload_code"));
        payload.setName(rs.getString("cv_payload_name"));
        payload.setType(rs.getString("cv_payload_type"));
        payload.setManufacturerOrganizationId((Integer) rs.getObject("ni_id_manufacturer_organization"));
        payload.setDescription(rs.getString("ct_description"));
        return payload;
    }

    private void fillStatement(PreparedStatement statement, Payload payload) throws SQLException {
        statement.setString(1, payload.getCode());
        statement.setString(2, payload.getName());
        statement.setString(3, payload.getType());
        statement.setObject(4, payload.getManufacturerOrganizationId());
        statement.setString(5, payload.getDescription());
    }
}