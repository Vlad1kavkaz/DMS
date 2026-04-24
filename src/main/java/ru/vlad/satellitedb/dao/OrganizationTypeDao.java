package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.OrganizationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrganizationTypeDao {

    public OrganizationType insert(OrganizationType type) {
        String sql = """
                INSERT INTO sc_cogs.tb_organization_type (
                    cv_organization_type,
                    ct_description
                ) VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, type);
            statement.executeUpdate();

            return type;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении типа организации", e);
        }
    }

    public boolean update(OrganizationType type, String oldType) {
        String sql = """
                UPDATE sc_cogs.tb_organization_type
                SET
                    cv_organization_type = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE cv_organization_type = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, type);
            statement.setString(3, oldType);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении типа организации type=" + oldType, e);
        }
    }

    public boolean deleteByType(String type) {
        String sql = "DELETE FROM sc_cogs.tb_organization_type WHERE cv_organization_type = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении типа организации type=" + type, e);
        }
    }

    public List<OrganizationType> findAll() {
        String sql = """
                SELECT
                    cv_organization_type,
                    ct_description
                FROM sc_cogs.tb_organization_type
                ORDER BY cv_organization_type
                """;

        List<OrganizationType> types = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                types.add(mapRow(rs));
            }

            return types;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении типов организаций", e);
        }
    }

    private OrganizationType mapRow(ResultSet rs) throws SQLException {
        OrganizationType type = new OrganizationType();
        type.setType(rs.getString("cv_organization_type"));
        type.setDescription(rs.getString("ct_description"));
        return type;
    }

    private void fillStatement(PreparedStatement statement, OrganizationType type) throws SQLException {
        statement.setString(1, type.getType());
        statement.setString(2, type.getDescription());
    }
}