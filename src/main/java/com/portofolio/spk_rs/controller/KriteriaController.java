package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.service.KriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kriteria")
@RequiredArgsConstructor
public class KriteriaController {

    private final KriteriaService service;

    @GetMapping
    public ResponseEntity<List<Kriteria>> getAll() {
        return ResponseEntity.ok(service.getAllKriteria());
    }

    @PostMapping
    public ResponseEntity<Kriteria> create(@RequestBody Kriteria kriteria) {
        return ResponseEntity.ok(service.createKriteria(kriteria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Kriteria> update(@PathVariable Long id, @RequestBody Kriteria kriteria) {
        return ResponseEntity.ok(service.updateKriteria(id, kriteria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteKriteria(id);
        return ResponseEntity.noContent().build();
    }
}