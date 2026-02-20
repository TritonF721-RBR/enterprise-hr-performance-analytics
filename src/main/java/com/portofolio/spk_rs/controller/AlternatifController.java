package com.portofolio.spk_rs.controller;

import com.portofolio.spk_rs.model.Alternatif;
import com.portofolio.spk_rs.model.Penilaian;
import com.portofolio.spk_rs.repository.AlternatifRepository;
import com.portofolio.spk_rs.repository.PenilaianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alternatif")
@RequiredArgsConstructor
public class AlternatifController {

    private final AlternatifRepository alternatifRepository;
    private final PenilaianRepository penilaianRepository;

    @GetMapping
    public ResponseEntity<List<Alternatif>> getAll() {
        return ResponseEntity.ok(alternatifRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Alternatif alternatif) {
        try {
            return ResponseEntity.ok(alternatifRepository.save(alternatif));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Gagal menyimpan data. Kode mungkin sudah ada.");
        }
    }

    // INI LOGIKA PENGHAPUSAN YANG BARU
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            // 1. Cari semua nilai rapor milik karyawan ini
            List<Penilaian> nilaiKaryawan = penilaianRepository.findAll().stream()
                    .filter(p -> p.getAlternatif().getId().equals(id))
                    .toList();

            // 2. Hapus nilainya dulu dari database (Bypass perlindungan Foreign Key)
            penilaianRepository.deleteAll(nilaiKaryawan);

            // 3. Setelah nilainya bersih, baru hapus data orangnya
            alternatifRepository.deleteById(id);

            return ResponseEntity.ok("Berhasil dihapus beserta nilainya");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gagal menghapus data: " + e.getMessage());
        }
    }
}