package ru.vlad.satellitedb.dao;

import ru.vlad.satellitedb.db.ConnectionFactory;
import ru.vlad.satellitedb.model.SatelliteSeries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SatelliteSeriesDao {

    public List<SatelliteSeries> findAll() {
        String sql = """
                SELECT
                    ni_id_satellite_series,
                    cv_satellite_series_code,
                    cv_satellite_series_name,
                    ct_description
                FROM sc_cogs.tb_satellite_series
                ORDER BY ni_id_satellite_series
                """;

        List<SatelliteSeries> seriesList = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                seriesList.add(mapRow(rs));
            }

            return seriesList;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка серий спутников", e);
        }
    }

    public SatelliteSeries insert(SatelliteSeries series) {
        String sql = """
                INSERT INTO sc_cogs.tb_satellite_series (
                    cv_satellite_series_code,
                    cv_satellite_series_name,
                    ct_description
                ) VALUES (?, ?, ?)
                RETURNING ni_id_satellite_series
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, series);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    series.setId(rs.getInt("ni_id_satellite_series"));
                }
            }

            return series;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении серии спутников", e);
        }
    }

    public boolean update(SatelliteSeries series) {
        String sql = """
                UPDATE sc_cogs.tb_satellite_series
                SET
                    cv_satellite_series_code = ?,
                    cv_satellite_series_name = ?,
                    ct_description = ?,
                    dt_updated_at = CURRENT_TIMESTAMP
                WHERE ni_id_satellite_series = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStatement(statement, series);
            statement.setInt(4, series.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении серии спутников id=" + series.getId(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM sc_cogs.tb_satellite_series WHERE ni_id_satellite_series = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении серии спутников id=" + id, e);
        }
    }

    private SatelliteSeries mapRow(ResultSet rs) throws SQLException {
        SatelliteSeries series = new SatelliteSeries();
        series.setId(rs.getInt("ni_id_satellite_series"));
        series.setCode(rs.getString("cv_satellite_series_code"));
        series.setName(rs.getString("cv_satellite_series_name"));
        series.setDescription(rs.getString("ct_description"));
        return series;
    }

    private void fillStatement(PreparedStatement statement, SatelliteSeries series) throws SQLException {
        statement.setString(1, series.getCode());
        statement.setString(2, series.getName());
        statement.setString(3, series.getDescription());
    }
}