package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.model.JenisKriteria;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.model.Penilaian;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import com.portofolio.spk_rs.repository.PenilaianRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class C45Service {

    private final RankingService rankingService;
    private final PenilaianRepository penilaianRepository;
    private final KriteriaRepository kriteriaRepository;

    @Data
    @Builder
    public static class C45Result {
        private String ruleGenerated;
        private List<C45Prediction> details;
    }

    @Data
    @Builder
    public static class C45Prediction {
        private String nama;
        private String statusAHP;
        private String prediksiC45;
        private String keterangan;
    }

    public C45Result analyzeExistingData() {
        var rankingData = rankingService.hitungPerankingan();
        var semuaNilai = penilaianRepository.findAll();
        var semuaKriteria = kriteriaRepository.findAll();

        if (rankingData.isEmpty()) {
            return C45Result.builder().ruleGenerated("Belum ada data evaluasi karyawan.").build();
        }

        // 1. TRAINING (Membuat Batas Terbaik & Terpilih yang SUPER KETAT)
        Map<String, Double> thTerbaik = new HashMap<>();
        Map<String, Double> thTerpilih = new HashMap<>();

        for (Kriteria k : semuaKriteria) {
            double totLulus = 0, totGagal = 0;
            long jmlLulus = 0, jmlGagal = 0;

            // Pisahkan nilai rata-rata kelompok Lulus AHP dan Gagal AHP
            for (var r : rankingData) {
                double val = getNilaiByKode(r.getAlternatifId(), k.getKode(), semuaNilai);
                if ("TERPILIH".equalsIgnoreCase(r.getStatus())) {
                    totLulus += val;
                    jmlLulus++;
                } else {
                    totGagal += val;
                    jmlGagal++;
                }
            }

            double avgLulus = (jmlLulus > 0) ? (totLulus / jmlLulus) : ((k.getJenis() == JenisKriteria.COST) ? 2.0 : 8.5);
            double avgGagal = (jmlGagal > 0) ? (totGagal / jmlGagal) : ((k.getJenis() == JenisKriteria.COST) ? 7.0 : 5.0);

            // LOGIKA BATAS BARU:
            // Terbaik = Harus menyamai rata-rata kelompok Terpilih.
            // Terpilih = Minimal ada di nilai tengah antara Lulus dan Gagal.
            double batasBaik = avgLulus;
            double batasPilih = (avgLulus + avgGagal) / 2.0;

            thTerbaik.put(k.getKode(), batasBaik);
            thTerpilih.put(k.getKode(), batasPilih);
        }

        // 2. GENERATE TEXT RULE
        String rule = "JIKA Kinerja menyamai Rata-rata Karyawan Unggulan MAKA TERBAIK. JIKA di Nilai Tengah MAKA TERPILIH. SELAIN ITU GAGAL.";

        // 3. TESTING PREDIKSI (Bagi jadi 3 Tingkatan yang Akurat)
        List<C45Prediction> details = new ArrayList<>();

        for (var r : rankingData) {
            boolean isTerbaik = true;
            boolean isTerpilih = true;
            List<String> alasan = new ArrayList<>();

            for (Kriteria k : semuaKriteria) {
                double nilaiUser = getNilaiByKode(r.getAlternatifId(), k.getKode(), semuaNilai);
                double batasBaik = thTerbaik.get(k.getKode());
                double batasPilih = thTerpilih.get(k.getKode());

                if (k.getJenis() == JenisKriteria.COST) {
                    // Semakin kecil semakin bagus (Absensi)
                    if (nilaiUser > batasBaik) isTerbaik = false;
                    if (nilaiUser > batasPilih) {
                        isTerpilih = false;
                        alasan.add(k.getKode() + " Terlalu Banyak (" + nilaiUser + ")");
                    }
                } else {
                    // Semakin besar semakin bagus (Kinerja)
                    if (nilaiUser < batasBaik) isTerbaik = false;
                    if (nilaiUser < batasPilih) {
                        isTerpilih = false;
                        alasan.add(k.getKode() + " Kurang (" + nilaiUser + ")");
                    }
                }
            }

            String hasilAI;
            String ket;

            // Jika satu kriteria saja anjlok di bawah nilai tengah, dia langsung GAGAL.
            if (isTerbaik) {
                hasilAI = "TERBAIK";
                ket = "Lolos Kualifikasi Atas (Performa Stabil Tinggi)";
            } else if (isTerpilih) {
                hasilAI = "TERPILIH";
                ket = "Lolos Kualifikasi Menengah (Aman)";
            } else {
                hasilAI = "GAGAL";
                ket = "Evaluasi: " + String.join(", ", alasan);
            }

            details.add(C45Prediction.builder()
                    .nama(r.getNama())
                    .statusAHP(r.getStatus())
                    .prediksiC45(hasilAI)
                    .keterangan(ket)
                    .build());
        }

        return C45Result.builder()
                .ruleGenerated(rule)
                .details(details)
                .build();
    }

    private double getNilaiByKode(Long altId, String kode, List<Penilaian> list) {
        return list.stream()
                .filter(p -> p.getAlternatif().getId().equals(altId) && p.getKriteria().getKode().equalsIgnoreCase(kode))
                .map(Penilaian::getNilai)
                .findFirst()
                .orElse(0.0);
    }
}