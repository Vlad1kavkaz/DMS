package ru.vlad.satellitedb.util;

public final class UiTextUtil {

    private UiTextUtil() {
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