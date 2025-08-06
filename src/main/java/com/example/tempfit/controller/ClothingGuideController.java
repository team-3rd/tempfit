// package com.example.tempfit.controller;

// import com.example.tempfit.entity.ClothMale;
// import com.example.tempfit.entity.ClothFemale;
// import com.example.tempfit.service.ClothingGuideService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;

// @RestController
// @RequestMapping("/api/v1/clothes")
// @RequiredArgsConstructor
// public class ClothingGuideController {

//     private final ClothingGuideService guideService;

//     /**
//      * 남성용 추천 의상 (category → ClothMale)
//      * 호출 예: GET /api/v1/clothes/male?temperature=21
//      */
//     @GetMapping("/male")
//     public ResponseEntity<Map<String, ClothMale>> recommendMale(
//             @RequestParam("temperature") int temperature) {

//         Map<String, ClothMale> recommendation =
//                 guideService.getRandomMaleClothingByTemperature(temperature);
//         return ResponseEntity.ok(recommendation);
//     }

//     /**
//      * 여성용 추천 의상 (category → ClothFemale)
//      * 호출 예: GET /api/v1/clothes/female?temperature=21
//      */
//     @GetMapping("/female")
//     public ResponseEntity<Map<String, ClothFemale>> recommendFemale(
//             @RequestParam("temperature") int temperature) {

//         Map<String, ClothFemale> recommendation =
//                 guideService.getRandomFemaleClothingByTemperature(temperature);
//         return ResponseEntity.ok(recommendation);
//     }
// }
