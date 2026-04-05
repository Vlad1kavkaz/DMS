package ru.vlad.satellitedb.ui;

public final class UiTextUtil {

    private UiTextUtil() {
    }

    public static String satellitePurpose(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "meteorology" -> "Метеорология";
            case "hydrology" -> "Гидрология";
            case "remote_sensing" -> "Дистанционное зондирование";
            case "climate_monitoring" -> "Климатический мониторинг";
            case "ocean_monitoring" -> "Мониторинг океана";
            case "ice_monitoring" -> "Мониторинг ледовой обстановки";
            case "environment_monitoring" -> "Экологический мониторинг";
            case "multi_purpose" -> "Многоцелевой";
            case "other" -> "Другое";
            default -> value;
        };
    }

    public static String satelliteStatus(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "planned" -> "Планируется";
            case "active" -> "Активен";
            case "reserve" -> "Резервный";
            case "inactive" -> "Неактивен";
            case "lost" -> "Потерян";
            case "retired" -> "Выведен из эксплуатации";
            default -> value;
        };
    }

    public static String organizationType(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "operator" -> "Оператор";
            case "owner" -> "Владелец";
            case "manufacturer" -> "Производитель";
            case "agency" -> "Агентство";
            case "other" -> "Другое";
            default -> value;
        };
    }

    public static String payloadType(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "radiometer" -> "Радиометр";
            case "spectrometer" -> "Спектрометр";
            case "imager" -> "Съёмочная аппаратура";
            case "radar" -> "Радар";
            case "relay" -> "Ретранслятор";
            case "scanner" -> "Сканер";
            case "other" -> "Другое";
            default -> value;
        };
    }

    public static String orbitType(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "geostationary" -> "Геостационарная";
            case "highly_elliptical" -> "Высокоэллиптическая";
            case "polar" -> "Полярная";
            default -> value;
        };
    }
}