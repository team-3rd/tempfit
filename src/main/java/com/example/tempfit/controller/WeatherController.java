package com.example.tempfit.controller;

import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.WeatherDTO;
import com.example.tempfit.service.WeatherService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    // 위도/경도로 현재 날씨 1개만 반환
    @GetMapping("/current")
    public List<WeatherDTO> getCurrentWeather(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon) {
        // 1. 위도/경도를 기상청 좌표로 변환
        CoordsDTO coords = new CoordsDTO();
        coords.setLat(lat);
        coords.setLon(lon);
        GridDTO grid = weatherService.changeCoords(coords);
        // 2. 해당 좌표의 날씨 가져오기
        List<WeatherDTO> weather = weatherService.getWeatherApi(grid);

        return weather;
    }
}
