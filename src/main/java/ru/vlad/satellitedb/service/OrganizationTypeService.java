package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.OrganizationTypeDao;
import ru.vlad.satellitedb.model.OrganizationType;

import java.util.List;

public class OrganizationTypeService {

    private final OrganizationTypeDao dao = new OrganizationTypeDao();

    public OrganizationType create(OrganizationType type) {
        validate(type);
        return dao.insert(type);
    }

    public boolean update(OrganizationType type, String oldType) {
        validate(type);

        if (oldType == null || oldType.isBlank()) {
            throw new IllegalArgumentException("Не выбран исходный тип организации");
        }

        return dao.update(type, oldType);
    }

    public boolean delete(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Не выбран тип организации");
        }

        return dao.deleteByType(type);
    }

    public List<OrganizationType> getAll() {
        return dao.findAll();
    }

    private void validate(OrganizationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип организации не заполнен");
        }

        if (type.getType() == null || type.getType().isBlank()) {
            throw new IllegalArgumentException("Введите тип организации");
        }

        if (type.getType().length() > 50) {
            throw new IllegalArgumentException("Тип организации не должен быть длиннее 50 символов");
        }
    }
}