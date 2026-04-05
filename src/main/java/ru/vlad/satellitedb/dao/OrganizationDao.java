package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.Organization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDao {

    public List<Organization> findAll() {
        String sql = """
                SELECT
                    ni_id_organization,
                    cv_organization_name,
                    cv_organization_short_name,
                    cv_organization_type,
                    cv_country_code,
                    cv_website
                FROM sc_cogs.tb_organization
                ORDER BY ni_id_organization
                """;

        List<Organization> organizations = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                organizations.add(mapRow(rs));
            }

            return organizations;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка организаций", e);
        }
    }

    public Organization insert(Organization organization) {
        String sql = """
                INSERT INTO sc_cogs.tb_organization (
                    cv_organization_name,
                    cv_organization_short_name,
                    cv_organization_type,
                    cv_country_code,
                    cv_website
                ) VALUES (?, ?, ?, ?, ?)
                RETURNING ni_id_organization
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, organization);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    organization.setId(rs.getInt("ni_id_organization"));
                }
            }

            return organization;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении организации", e);
        }
    }

    public boolean update(Organization organization) {
        String sql = """
                UPDATE sc_cogs.tb_organization
                SET
                    cv_organization_name = ?,
                    cv_organization_short_name = ?,
                    cv_organization_type = ?,
                    cv_country_code = ?,
                    cv_website = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE ni_id_organization = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, organization);
            statement.setInt(6, organization.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении организации id=" + organization.getId(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM sc_cogs.tb_organization WHERE ni_id_organization = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении организации id=" + id, e);
        }
    }

    private Organization mapRow(ResultSet rs) throws SQLException {
        Organization organization = new Organization();
        organization.setId(rs.getInt("ni_id_organization"));
        organization.setName(rs.getString("cv_organization_name"));
        organization.setShortName(rs.getString("cv_organization_short_name"));
        organization.setType(rs.getString("cv_organization_type"));
        organization.setCountryCode(rs.getString("cv_country_code"));
        organization.setWebsite(rs.getString("cv_website"));
        return organization;
    }

    private void fillStatement(PreparedStatement statement, Organization organization) throws SQLException {
        statement.setString(1, organization.getName());
        statement.setString(2, organization.getShortName());
        statement.setString(3, organization.getType());
        statement.setString(4, organization.getCountryCode());
        statement.setString(5, organization.getWebsite());
    }
}