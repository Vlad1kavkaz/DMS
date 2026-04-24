package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.PayloadTypeDao;
import ru.vlad.satellitedb.model.PayloadType;

import java.util.List;

public class PayloadTypeService {

    private final PayloadTypeDao dao = new PayloadTypeDao();

    public PayloadType create(PayloadType type) {
        validate(type);
        return dao.insert(type);
    }

    public boolean update(PayloadType type, String oldType) {
        validate(type);

        if (oldType == null || oldType.isBlank()) {
            throw new IllegalArgumentException("Не выбран исходный тип полезной нагрузки");
        }

        return dao.update(type, oldType);
    }

    public boolean delete(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Не выбран тип полезной нагрузки");
        }

        return dao.deleteByType(type);
    }

    public List<PayloadType> getAll() {
        return dao.findAll();
    }

    private void validate(PayloadType type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип полезной нагрузки не заполнен");
        }

        if (type.getType() == null || type.getType().isBlank()) {
            throw new IllegalArgumentException("Введите тип полезной нагрузки");
        }

        if (type.getType().length() > 50) {
            throw new IllegalArgumentException("Тип полезной нагрузки не должен быть длиннее 50 символов");
        }
    }
}