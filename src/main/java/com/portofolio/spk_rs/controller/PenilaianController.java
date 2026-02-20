package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.dto.PenilaianRequest;
import com.portofolio.spk_rs.model.Penilaian;
import com.portofolio.spk_rs.service.PenilaianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penilaian")
@RequiredArgsConstructor
public class PenilaianController {

    private final PenilaianService service;

    // POST: Input Nilai
    @PostMapping
    public ResponseEntity<Penilaian> inputNilai(@RequestBody PenilaianRequest request) {
        return ResponseEntity.ok(service.inputNilai(request));
    }

    // GET: Lihat Nilai berdasarkan ID Karyawan (Alternatif)
    // Contoh URL: /api/penilaian/alternatif/1
    @GetMapping("/alternatif/{alternatifId}")
    public ResponseEntity<List<Penilaian>> getNilaiByAlternatif(@PathVariable Long alternatifId) {
        return ResponseEntity.ok(service.getNilaiByAlternatif(alternatifId));
    }
}