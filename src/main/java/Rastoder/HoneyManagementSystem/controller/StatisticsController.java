// src/main/java/Rastoder/HoneyManagementSystem/controller/StatisticsController.java
package Rastoder.HoneyManagementSystem.controller;

import Rastoder.HoneyManagementSystem.dto.response.DashboardStatisticsResponse;
import Rastoder.HoneyManagementSystem.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;



    @GetMapping
    public ResponseEntity<DashboardStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getDashboardStatistics());
    }
}