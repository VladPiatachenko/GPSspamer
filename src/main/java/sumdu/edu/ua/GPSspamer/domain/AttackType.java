package sumdu.edu.ua.GPSspamer.domain;

public enum AttackType {
    NONE,
    BIAS,      // сталий зсув (метри)
    DRIFT,     // дрейф з часом (м/с)
    NOISE      // шум (sigma в метрах)
}
