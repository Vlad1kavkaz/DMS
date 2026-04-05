package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.SatelliteSeriesDao;
import ru.vlad.satellitedb.model.SatelliteSeries;

import java.util.List;

public class SatelliteSeriesService {

    private final SatelliteSeriesDao satelliteSeriesDao = new SatelliteSeriesDao();

    // =========================
    // UI-friendly API
    // =========================

    public List<SatelliteSeries> getAll() {
        return getAllSeries();
    }

    public SatelliteSeries create(SatelliteSeries series) {
        return createSeries(series);
    }

    public boolean update(SatelliteSeries series) {
        return updateSeries(series);
    }

    public boolean delete(int id) {
        return deleteSeries(id);
    }

    // =========================
    // Existing API
    // =========================

    public List<SatelliteSeries> getAllSeries() {
        return satelliteSeriesDao.findAll();
    }

    public SatelliteSeries createSeries(SatelliteSeries series) {
        validateSeries(series, false);
        normalizeSeries(series);
        return satelliteSeriesDao.insert(series);
    }

    public boolean updateSeries(SatelliteSeries series) {
        validateSeries(series, true);
        normalizeSeries(series);
        return satelliteSeriesDao.update(series);
    }

    public boolean deleteSeries(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id серии спутников");
        }
        return satelliteSeriesDao.deleteById(id);
    }

    private void validateSeries(SatelliteSeries series, boolean idRequired) {
        if (series == null) {
            throw new IllegalArgumentException("Серия спутников не должна быть null");
        }

        if (idRequired && (series.getId() == null || series.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id");
        }

        if (series.getCode() == null || series.getCode().isBlank()) {
            throw new IllegalArgumentException("Код серии спутников обязателен");
        }

        if (series.getName() == null || series.getName().isBlank()) {
            throw new IllegalArgumentException("Название серии спутников обязательно");
        }
    }

    private void normalizeSeries(SatelliteSeries series) {
        series.setCode(trimToNull(series.getCode()));
        series.setName(trimToNull(series.getName()));
        series.setDescription(trimToNull(series.getDescription()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}