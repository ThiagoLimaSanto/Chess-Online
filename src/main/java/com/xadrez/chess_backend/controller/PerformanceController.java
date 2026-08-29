package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.Performance.PerformanceResponse;
import com.xadrez.chess_backend.service.PerformanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/{userId}")
    public ResponseEntity<PerformanceResponse> getPerformance(@PathVariable UUID userId) {
        return ResponseEntity.ok(performanceService.getPerformance(userId));
    }
}
