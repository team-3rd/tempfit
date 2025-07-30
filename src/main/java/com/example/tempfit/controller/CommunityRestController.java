package com.example.tempfit.controller;

import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityRestController {
    private final CommunityService communityService;

    @PostMapping("/view/{id}")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        communityService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}
