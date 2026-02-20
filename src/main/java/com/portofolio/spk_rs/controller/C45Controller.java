package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.service.C45Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/c45")
@RequiredArgsConstructor
public class C45Controller {

    private final C45Service c45Service;

    // Endpoint baru: Tinggal klik, langsung keluar hasil analisanya
    @GetMapping("/analyze")
    public ResponseEntity<C45Service.C45Result> analyze() {
        return ResponseEntity.ok(c45Service.analyzeExistingData());
    }
}