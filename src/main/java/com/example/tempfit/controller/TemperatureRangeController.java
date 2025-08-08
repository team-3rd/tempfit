package com.example.tempfit.controller;

import com.example.tempfit.dto.TemperatureRangeDTO;
import com.example.tempfit.entity.TemperatureRange;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TemperatureRangeController {


    // enum TemperatureRange 의 모든 값을 code/min/max 만 뽑아서 JSON 배열로 반환 
    @GetMapping("/api/temperature/ranges")
    public List<TemperatureRangeDTO> getAllRanges() {
        return Arrays.stream(TemperatureRange.values())
            .map(r -> new TemperatureRangeDTO(r.getCode(), r.getMinTemp(), r.getMaxTemp()))
            .collect(Collectors.toList());
    }
}
