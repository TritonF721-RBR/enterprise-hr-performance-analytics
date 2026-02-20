package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.dto.PenilaianRequest;
import com.portofolio.spk_rs.model.Alternatif;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.model.Penilaian;
import com.portofolio.spk_rs.repository.AlternatifRepository;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import com.portofolio.spk_rs.repository.PenilaianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PenilaianService {

    private final PenilaianRepository penilaianRepository;
    private final AlternatifRepository alternatifRepository;
    private final KriteriaRepository kriteriaRepository;

    // 1. Fungsi Input Nilai (Simpan atau Update)
    public Penilaian inputNilai(PenilaianRequest request) {
        // Cek dulu, Alternatif (Orangnya) ada gak?
        Alternatif alternatif = alternatifRepository.findById(request.getAlternatifId())
                .orElseThrow(() -> new RuntimeException("Alternatif tidak ditemukan!"));

        // Cek dulu, Kriterianya ada gak?
        Kriteria kriteria = kriteriaRepository.findById(request.getKriteriaId())
                .orElseThrow(() -> new RuntimeException("Kriteria tidak ditemukan!"));

        // Cek apakah nilai untuk (Orang ini + Kriteria ini) sudah pernah diisi?
        Optional<Penilaian> existingPenilaian = penilaianRepository
                .findByAlternatifAndKriteria(alternatif, kriteria);

        Penilaian penilaian;
        if (existingPenilaian.isPresent()) {
            // Kalau sudah ada, kita UPDATE nilainya
            penilaian = existingPenilaian.get();
            penilaian.setNilai(request.getNilai());
        } else {
            // Kalau belum ada, kita BUAT BARU
            penilaian = Penilaian.builder()
                    .alternatif(alternatif)
                    .kriteria(kriteria)
                    .nilai(request.getNilai())
                    .build();
        }

        return penilaianRepository.save(penilaian);
    }

    // 2. Ambil Semua Nilai milik Satu Orang (Buat nampilin rapor)
    public List<Penilaian> getNilaiByAlternatif(Long alternatifId) {
        Alternatif alternatif = alternatifRepository.findById(alternatifId)
                .orElseThrow(() -> new RuntimeException("Alternatif tidak ditemukan!"));

        return penilaianRepository.findByAlternatif(alternatif);
    }
}