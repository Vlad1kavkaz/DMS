package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.CrossSatellitePayloadDao;
import ru.vlad.satellitedb.model.CrossSatellitePayload;

import java.util.List;

public class CrossSatellitePayloadService {

    private final CrossSatellitePayloadDao crossSatellitePayloadDao = new CrossSatellitePayloadDao();

    public void createLink(CrossSatellitePayload link) {
        validateLink(link, false);
        crossSatellitePayloadDao.insert(link);
    }

    public void updateLink(CrossSatellitePayload link) {
        validateLink(link, true);
        crossSatellitePayloadDao.update(link);
    }

    public void deleteLink(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id связи");
        }
        crossSatellitePayloadDao.deleteById(id);
    }

    public List<CrossSatellitePayload> getLinksBySatelliteId(int satelliteId) {
        if (satelliteId <= 0) {
            throw new IllegalArgumentException("Некорректный satelliteId");
        }
        return crossSatellitePayloadDao.findBySatelliteId(satelliteId);
    }

    private void validateLink(CrossSatellitePayload link, boolean idRequired) {
        if (link == null) {
            throw new IllegalArgumentException("Связь не должна быть null");
        }

        if (idRequired && (link.getId() == null || link.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id");
        }

        if (link.getSatelliteId() == null || link.getSatelliteId() <= 0) {
            throw new IllegalArgumentException("Некорректный satelliteId");
        }

        if (link.getPayloadId() == null || link.getPayloadId() <= 0) {
            throw new IllegalArgumentException("Некорректный payloadId");
        }
    }
}