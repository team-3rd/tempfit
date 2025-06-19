package com.example.tempfit.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.WeatherDTO;
import com.example.tempfit.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@RequiredArgsConstructor
@RequestMapping("/weathers")
@RestController
public class MainRestController {

    private final WeatherService weatherService;

    @PostMapping("/coords")
    public List<WeatherDTO> postCoords(@RequestBody CoordsDTO cDto) {
        log.info("좌표 요청");
        GridDTO dto = weatherService.changeCoords(cDto);
        return weatherService.getWeatherApi(dto);
    }

    // @GetMapping("/mains")
    // public List<WeatherDTO> getMains(GridDTO dto) {
    // log.info("페이지 요청");
    // return weatherService.getWeatherApi(dto);
    // }
}
