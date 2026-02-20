package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KriteriaService {

    private final KriteriaRepository repository;

    // 1. Ambil Semua Data
    public List<Kriteria> getAllKriteria() {
        return repository.findAll();
    }

    // 2. Tambah Data Baru
    public Kriteria createKriteria(Kriteria kriteria) {
        if (repository.existsByKode(kriteria.getKode())) {
            throw new RuntimeException("Kode kriteria " + kriteria.getKode() + " sudah ada!");
        }
        return repository.save(kriteria);
    }

    // 3. Update Data
    public Kriteria updateKriteria(Long id, Kriteria kriteriaBaru) {
        Kriteria kriteriaLama = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kriteria tidak ditemukan"));

        kriteriaLama.setNama(kriteriaBaru.getNama());
        kriteriaLama.setJenis(kriteriaBaru.getJenis());
        // Kode & Bobot biasanya tidak diubah sembarangan, tapi kalau mau bisa ditambah disini

        return repository.save(kriteriaLama);
    }

    // 4. Hapus Data
    public void deleteKriteria(Long id) {
        repository.deleteById(id);
    }
}