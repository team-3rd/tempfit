package com.example.tempfit.dto;

public class TemperatureRangeDTO {
    private int code;
    private int minTemp;
    private int maxTemp;

    public TemperatureRangeDTO(int code, int minTemp, int maxTemp) {
        this.code = code;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public int getCode() { return code; }
    public int getMinTemp() { return minTemp; }
    public int getMaxTemp() { return maxTemp; }
}
