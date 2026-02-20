package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.dto.AhpRequest;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.service.AhpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ahp")
@RequiredArgsConstructor
public class AhpController {

    private final AhpService ahpService;

    @PostMapping("/hitung-bobot")
    public ResponseEntity<List<Kriteria>> hitungBobot(@RequestBody AhpRequest request) {
        return ResponseEntity.ok(ahpService.hitungDanSimpanBobot(request));
    }
}