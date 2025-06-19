package com.example.tempfit.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {

    private String pty;
    private String sky;
    private String tmp;

    private LocalDate fcstDate;
    private LocalTime fcstTime;

    private String temp;

}
