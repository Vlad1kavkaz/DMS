package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.PayloadDao;
import ru.vlad.satellitedb.model.Payload;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PayloadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "radiometer",
            "spectrometer",
            "imager",
            "radar",
            "relay",
            "scanner",
            "other"
    );

    private final PayloadDao payloadDao = new PayloadDao();

    // =========================
    // UI-friendly API
    // =========================

    public List<Payload> getAll() {
        return getAllPayloads();
    }

    public Optional<Payload> getById(int id) {
        return getPayloadById(id);
    }

    public Payload create(Payload payload) {
        return createPayload(payload);
    }

    public boolean update(Payload payload) {
        return updatePayload(payload);
    }

    public boolean delete(int id) {
        return deletePayload(id);
    }

    // =========================
    // Existing API
    // =========================

    public List<Payload> getAllPayloads() {
        return payloadDao.findAll();
    }

    public Optional<Payload> getPayloadById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id полезной нагрузки");
        }
        return payloadDao.findById(id);
    }

    public Payload createPayload(Payload payload) {
        validatePayload(payload, false);
        normalizePayload(payload);
        return payloadDao.insert(payload);
    }

    public boolean updatePayload(Payload payload) {
        validatePayload(payload, true);
        normalizePayload(payload);
        return payloadDao.update(payload);
    }

    public boolean deletePayload(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id полезной нагрузки");
        }
        return payloadDao.deleteById(id);
    }

    public Payload requireById(int id) {
        return getById(id).orElseThrow(() ->
                new IllegalArgumentException("Полезная нагрузка не найдена, id=" + id));
    }

    private void validatePayload(Payload payload, boolean idRequired) {
        if (payload == null) {
            throw new IllegalArgumentException("Полезная нагрузка не должна быть null");
        }

        if (idRequired && (payload.getId() == null || payload.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id");
        }

        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new IllegalArgumentException("Название полезной нагрузки обязательно");
        }

        if (payload.getType() == null || !ALLOWED_TYPES.contains(payload.getType().trim().toLowerCase())) {
            throw new IllegalArgumentException("Некорректный тип полезной нагрузки");
        }
    }

    private void normalizePayload(Payload payload) {
        payload.setCode(trimToNull(payload.getCode()));
        payload.setName(trimToNull(payload.getName()));
        payload.setType(normalizeType(payload.getType()));
        payload.setDescription(trimToNull(payload.getDescription()));
    }

    private String normalizeType(String type) {
        return type == null ? null : type.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}