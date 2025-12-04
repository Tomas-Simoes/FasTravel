package ubi.pdm.fastravel.frontend;

public class CalcularCO2 {

    // g de CO2 por KM
    public static final double CARRO = 120;
    public static final double BUS = 80;
    public static final double COMBOIO = 30;
    public static final double METRO = 25;
    public static final double ANDAR = 0;

    private static double metersToKm(double meters) {
        return meters / 1000.0;
    }

    // Calcula CO2
    public static double calculateSegmentCO2(String mode, double distanceMeters) {
        double km = metersToKm(distanceMeters);

        switch (mode) {
            case "CARRO":
                return km * CARRO;

            case "BUS":
                return km * BUS;

            case "COMBOIO":
                return km * COMBOIO;

            case "METRO":
                return km * METRO;

            case "WALKING":
            default:
                return km * ANDAR;
        }
    }
}