package ru.vlad.satellitedb.service;

import ru.vlad.satellitedb.dao.OrganizationDao;
import ru.vlad.satellitedb.model.Organization;

import java.util.List;
import java.util.Set;

public class OrganizationService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "operator",
            "owner",
            "manufacturer",
            "agency",
            "other"
    );

    private final OrganizationDao organizationDao = new OrganizationDao();

    // =========================
    // UI-friendly API
    // =========================

    public List<Organization> getAll() {
        return getAllOrganizations();
    }

    public Organization create(Organization organization) {
        return createOrganization(organization);
    }

    public boolean update(Organization organization) {
        return updateOrganization(organization);
    }

    public boolean delete(int id) {
        return deleteOrganization(id);
    }

    // =========================
    // Existing API
    // =========================

    public List<Organization> getAllOrganizations() {
        return organizationDao.findAll();
    }

    public Organization createOrganization(Organization organization) {
        validateOrganization(organization, false);
        normalizeOrganization(organization);
        return organizationDao.insert(organization);
    }

    public boolean updateOrganization(Organization organization) {
        validateOrganization(organization, true);
        normalizeOrganization(organization);
        return organizationDao.update(organization);
    }

    public boolean deleteOrganization(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некорректный id организации");
        }
        return organizationDao.deleteById(id);
    }

    private void validateOrganization(Organization organization, boolean idRequired) {
        if (organization == null) {
            throw new IllegalArgumentException("Организация не должна быть null");
        }

        if (idRequired && (organization.getId() == null || organization.getId() <= 0)) {
            throw new IllegalArgumentException("Для обновления требуется корректный id");
        }

        if (organization.getName() == null || organization.getName().isBlank()) {
            throw new IllegalArgumentException("Название организации обязательно");
        }

        if (organization.getType() == null || !ALLOWED_TYPES.contains(organization.getType().trim().toLowerCase())) {
            throw new IllegalArgumentException("Некорректный тип организации");
        }

        if (organization.getCountryCode() == null || organization.getCountryCode().isBlank()) {
            throw new IllegalArgumentException("Код страны обязателен");
        }

        if (organization.getCountryCode().trim().length() != 2) {
            throw new IllegalArgumentException("Код страны должен состоять из 2 символов");
        }
    }

    private void normalizeOrganization(Organization organization) {
        organization.setName(trimToNull(organization.getName()));
        organization.setShortName(trimToNull(organization.getShortName()));
        organization.setType(normalizeType(organization.getType()));
        organization.setCountryCode(normalizeCountryCode(organization.getCountryCode()));
        organization.setWebsite(trimToNull(organization.getWebsite()));
    }

    private String normalizeType(String type) {
        return type == null ? null : type.trim().toLowerCase();
    }

    private String normalizeCountryCode(String countryCode) {
        return countryCode == null ? null : countryCode.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}