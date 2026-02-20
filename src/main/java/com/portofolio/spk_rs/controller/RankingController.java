package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.dto.RankingResponse;
import com.portofolio.spk_rs.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingResponse>> getLeaderboard() {
        return ResponseEntity.ok(rankingService.hitungPerankingan());
    }
}