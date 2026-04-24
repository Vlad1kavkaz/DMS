package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.SatellitePurposeDao;
import ru.vlad.satellitedb.model.SatellitePurpose;

import java.util.List;

public class SatellitePurposeService {

    private final SatellitePurposeDao dao = new SatellitePurposeDao();

    public SatellitePurpose create(SatellitePurpose purpose) {
        validate(purpose);
        return dao.insert(purpose);
    }

    public boolean update(SatellitePurpose purpose, String oldPurpose) {
        validate(purpose);

        if (oldPurpose == null || oldPurpose.isBlank()) {
            throw new IllegalArgumentException("Не выбрано исходное назначение спутника");
        }

        return dao.update(purpose, oldPurpose);
    }

    public boolean delete(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Не выбрано назначение спутника");
        }

        return dao.deleteByPurpose(purpose);
    }

    public List<SatellitePurpose> getAll() {
        return dao.findAll();
    }

    private void validate(SatellitePurpose purpose) {
        if (purpose == null) {
            throw new IllegalArgumentException("Назначение спутника не заполнено");
        }

        if (purpose.getPurpose() == null || purpose.getPurpose().isBlank()) {
            throw new IllegalArgumentException("Введите назначение спутника");
        }

        if (purpose.getPurpose().length() > 50) {
            throw new IllegalArgumentException("Назначение спутника не должно быть длиннее 50 символов");
        }
    }
}