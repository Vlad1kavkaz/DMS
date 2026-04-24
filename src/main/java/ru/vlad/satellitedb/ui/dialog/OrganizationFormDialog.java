package ru.vlad.satellitedb.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import ru.vlad.satellitedb.model.Organization;
import ru.vlad.satellitedb.model.OrganizationType;
import ru.vlad.satellitedb.service.OrganizationTypeService;

import java.util.Optional;
import java.util.stream.Collectors;

public class OrganizationFormDialog {

    private final Dialog<Organization> dialog = new Dialog<>();

    private final TextField nameField = new TextField();
    private final TextField shortNameField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final TextField countryCodeField = new TextField();
    private final TextField websiteField = new TextField();

    private final OrganizationTypeService organizationTypeService = new OrganizationTypeService();
    private final Organization organization;

    public OrganizationFormDialog(Organization organization) {
        this.organization = organization != null ? organization : new Organization();

        dialog.setTitle(organization == null ? "Добавление организации" : "Редактирование организации");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        dialog.getDialogPane().setContent(buildForm());

        fillChoices();
        fillFields();

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return buildOrganizationFromForm();
            }
            return null;
        });
    }

    public Optional<Organization> showAndWaitForResult() {
        return dialog.showAndWait();
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        int row = 0;

        grid.add(new Label("Название:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Краткое название:"), 0, row);
        grid.add(shortNameField, 1, row++);

        grid.add(new Label("Тип:"), 0, row);
        grid.add(typeBox, 1, row++);

        grid.add(new Label("Код страны:"), 0, row);
        grid.add(countryCodeField, 1, row++);

        grid.add(new Label("Сайт:"), 0, row);
        grid.add(websiteField, 1, row);

        return grid;
    }

    private void fillChoices() {
        typeBox.getItems().setAll(
                organizationTypeService.getAll().stream()
                        .map(OrganizationType::getType)
                        .collect(Collectors.toList())
        );

        typeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return value != null ? value : "";
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    private void fillFields() {
        nameField.setText(organization.getName());
        shortNameField.setText(organization.getShortName());
        typeBox.setValue(organization.getType());
        countryCodeField.setText(organization.getCountryCode());
        websiteField.setText(organization.getWebsite());
    }

    private Organization buildOrganizationFromForm() {
        organization.setName(trimToNull(nameField.getText()));
        organization.setShortName(trimToNull(shortNameField.getText()));
        organization.setType(typeBox.getValue());
        organization.setCountryCode(trimToNull(countryCodeField.getText()));
        organization.setWebsite(trimToNull(websiteField.getText()));
        return organization;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}