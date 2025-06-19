package com.example.tempfit.entity;

import lombok.Getter;

@Getter
public enum TemperatureRange {
    FREEZING(1, -100, 4),
    VERY_COLD(2, 5, 8),
    COLD(3, 9, 11),
    COOL(4, 12, 16),
    MILD(5, 17, 19),
    WARM(6, 20, 22),
    HOT(7, 23, 27),
    VERY_HOT(8, 28, 100);

    private final int code;
    private final int minTemp;
    private final int maxTemp;

    TemperatureRange(int code, int minTemp, int maxTemp) {
        this.code = code;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public static TemperatureRange fromTemperature(int temp) {
        for (TemperatureRange range : values()) {
            if (temp >= range.minTemp && temp <= range.maxTemp) {
                return range;
            }
        }
        throw new IllegalArgumentException("해당 온도에 맞는 범위 없음: " + temp);
    }
}
