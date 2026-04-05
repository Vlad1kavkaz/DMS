package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.Satellite;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SatelliteDao {

    public List<Satellite> findAll() {
        String sql = """
                SELECT
                    ni_id_satellite,
                    ni_id_satellite_series,
                    cv_satellite_name,
                    cv_satellite_code,
                    cv_international_designator,
                    ni_norad_catalog_number,
                    cv_satellite_purpose,
                    cv_satellite_status,
                    dt_launch_date,
                    dt_operation_start_date,
                    dt_decommission_date,
                    ni_id_operator_organization,
                    ni_id_owner_organization,
                    ni_id_manufacturer_organization,
                    ct_description,
                    ct_notes
                FROM sc_cogs.tb_satellite
                ORDER BY ni_id_satellite
                """;

        List<Satellite> satellites = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                satellites.add(mapRow(rs));
            }

            return satellites;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка спутников", e);
        }
    }

    public Optional<Satellite> findById(int id) {
        String sql = """
                SELECT
                    ni_id_satellite,
                    ni_id_satellite_series,
                    cv_satellite_name,
                    cv_satellite_code,
                    cv_international_designator,
                    ni_norad_catalog_number,
                    cv_satellite_purpose,
                    cv_satellite_status,
                    dt_launch_date,
                    dt_operation_start_date,
                    dt_decommission_date,
                    ni_id_operator_organization,
                    ni_id_owner_organization,
                    ni_id_manufacturer_organization,
                    ct_description,
                    ct_notes
                FROM sc_cogs.tb_satellite
                WHERE ni_id_satellite = ?
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
            throw new RuntimeException("Ошибка при получении спутника по id=" + id, e);
        }
    }

    public Satellite insert(Satellite satellite) {
        String sql = """
                INSERT INTO sc_cogs.tb_satellite (
                    ni_id_satellite_series,
                    cv_satellite_name,
                    cv_satellite_code,
                    cv_international_designator,
                    ni_norad_catalog_number,
                    cv_satellite_purpose,
                    cv_satellite_status,
                    dt_launch_date,
                    dt_operation_start_date,
                    dt_decommission_date,
                    ni_id_operator_organization,
                    ni_id_owner_organization,
                    ni_id_manufacturer_organization,
                    ct_description,
                    ct_notes
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING ni_id_satellite
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, satellite);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    satellite.setId(rs.getInt("ni_id_satellite"));
                }
            }

            return satellite;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении спутника", e);
        }
    }

    public boolean update(Satellite satellite) {
        String sql = """
                UPDATE sc_cogs.tb_satellite
                SET
                    ni_id_satellite_series = ?,
                    cv_satellite_name = ?,
                    cv_satellite_code = ?,
                    cv_international_designator = ?,
                    ni_norad_catalog_number = ?,
                    cv_satellite_purpose = ?,
                    cv_satellite_status = ?,
                    dt_launch_date = ?,
                    dt_operation_start_date = ?,
                    dt_decommission_date = ?,
                    ni_id_operator_organization = ?,
                    ni_id_owner_organization = ?,
                    ni_id_manufacturer_organization = ?,
                    ct_description = ?,
                    ct_notes = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE ni_id_satellite = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, satellite);
            statement.setInt(16, satellite.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении спутника id=" + satellite.getId(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM sc_cogs.tb_satellite WHERE ni_id_satellite = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении спутника id=" + id, e);
        }
    }

    private Satellite mapRow(ResultSet rs) throws SQLException {
        Satellite satellite = new Satellite();

        satellite.setId(rs.getInt("ni_id_satellite"));
        satellite.setSatelliteSeriesId((Integer) rs.getObject("ni_id_satellite_series"));
        satellite.setName(rs.getString("cv_satellite_name"));
        satellite.setCode(rs.getString("cv_satellite_code"));
        satellite.setInternationalDesignator(rs.getString("cv_international_designator"));
        satellite.setNoradCatalogNumber((Integer) rs.getObject("ni_norad_catalog_number"));
        satellite.setPurpose(rs.getString("cv_satellite_purpose"));
        satellite.setStatus(rs.getString("cv_satellite_status"));
        satellite.setLaunchDate(toLocalDate(rs.getDate("dt_launch_date")));
        satellite.setOperationStartDate(toLocalDate(rs.getDate("dt_operation_start_date")));
        satellite.setDecommissionDate(toLocalDate(rs.getDate("dt_decommission_date")));
        satellite.setOperatorOrganizationId((Integer) rs.getObject("ni_id_operator_organization"));
        satellite.setOwnerOrganizationId((Integer) rs.getObject("ni_id_owner_organization"));
        satellite.setManufacturerOrganizationId((Integer) rs.getObject("ni_id_manufacturer_organization"));
        satellite.setDescription(rs.getString("ct_description"));
        satellite.setNotes(rs.getString("ct_notes"));

        return satellite;
    }

    private void fillStatement(PreparedStatement statement, Satellite satellite) throws SQLException {
        statement.setObject(1, satellite.getSatelliteSeriesId());
        statement.setString(2, satellite.getName());
        statement.setString(3, satellite.getCode());
        statement.setString(4, satellite.getInternationalDesignator());
        statement.setObject(5, satellite.getNoradCatalogNumber());
        statement.setString(6, satellite.getPurpose());
        statement.setString(7, satellite.getStatus());
        statement.setDate(8, toSqlDate(satellite.getLaunchDate()));
        statement.setDate(9, toSqlDate(satellite.getOperationStartDate()));
        statement.setDate(10, toSqlDate(satellite.getDecommissionDate()));
        statement.setObject(11, satellite.getOperatorOrganizationId());
        statement.setObject(12, satellite.getOwnerOrganizationId());
        statement.setObject(13, satellite.getManufacturerOrganizationId());
        statement.setString(14, satellite.getDescription());
        statement.setString(15, satellite.getNotes());
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private Date toSqlDate(java.time.LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }
}