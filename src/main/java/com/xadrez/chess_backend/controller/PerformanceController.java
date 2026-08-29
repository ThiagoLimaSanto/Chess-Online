package com.xadrez.chess_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.service.PerformanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class PerformanceController {
    
    private final PerformanceService performanceService;
}
