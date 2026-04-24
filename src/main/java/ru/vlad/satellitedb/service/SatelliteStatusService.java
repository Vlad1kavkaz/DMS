package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.SatelliteStatusDao;
import ru.vlad.satellitedb.model.SatelliteStatus;

import java.util.List;

public class SatelliteStatusService {

    private final SatelliteStatusDao dao = new SatelliteStatusDao();

    public SatelliteStatus create(SatelliteStatus status) {
        validate(status);
        return dao.insert(status);
    }

    public boolean update(SatelliteStatus status, String oldStatus) {
        validate(status);

        if (oldStatus == null || oldStatus.isBlank()) {
            throw new IllegalArgumentException("Не выбран исходный статус спутника");
        }

        return dao.update(status, oldStatus);
    }

    public boolean delete(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Не выбран статус спутника");
        }

        return dao.deleteByStatus(status);
    }

    public List<SatelliteStatus> getAll() {
        return dao.findAll();
    }

    private void validate(SatelliteStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус спутника не заполнен");
        }

        if (status.getStatus() == null || status.getStatus().isBlank()) {
            throw new IllegalArgumentException("Введите статус спутника");
        }

        if (status.getStatus().length() > 50) {
            throw new IllegalArgumentException("Статус спутника не должен быть длиннее 50 символов");
        }
    }
}