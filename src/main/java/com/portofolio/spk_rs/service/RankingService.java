package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.dto.RankingResponse;
import com.portofolio.spk_rs.model.Alternatif;
import com.portofolio.spk_rs.model.JenisKriteria;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.model.Penilaian;
import com.portofolio.spk_rs.repository.AlternatifRepository;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import com.portofolio.spk_rs.repository.PenilaianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final AlternatifRepository alternatifRepository;
    private final KriteriaRepository kriteriaRepository;
    private final PenilaianRepository penilaianRepository;

    public List<RankingResponse> hitungPerankingan() {
        List<Alternatif> listAlternatif = alternatifRepository.findAll();
        List<Kriteria> listKriteria = kriteriaRepository.findAll();
        List<Penilaian> listPenilaian = penilaianRepository.findAll();

        Map<Long, Double> maxValues = new HashMap<>();
        Map<Long, Double> minValues = new HashMap<>();

        // 1. Cari Max/Min
        for (Kriteria k : listKriteria) {
            double max = 0;
            double min = Double.MAX_VALUE;
            for (Penilaian p : listPenilaian) {
                if (p.getKriteria().getId().equals(k.getId())) {
                    if (p.getNilai() > max) max = p.getNilai();
                    if (p.getNilai() < min) min = p.getNilai();
                }
            }
            maxValues.put(k.getId(), max);
            minValues.put(k.getId(), min);
        }

        // 2. Hitung Skor & Tentukan 2 Status (AHP)
        List<RankingResponse> leaderboard = new ArrayList<>();

        for (Alternatif a : listAlternatif) {
            double totalSkor = 0;

            for (Kriteria k : listKriteria) {
                double nilaiAsli = 0;
                for (Penilaian p : listPenilaian) {
                    if (p.getAlternatif().getId().equals(a.getId()) && p.getKriteria().getId().equals(k.getId())) {
                        nilaiAsli = p.getNilai();
                        break;
                    }
                }

                double nilaiNormalisasi = 0;
                if (k.getJenis() == JenisKriteria.BENEFIT) {
                    double max = maxValues.getOrDefault(k.getId(), 1.0);
                    nilaiNormalisasi = nilaiAsli / (max == 0 ? 1 : max);
                } else {
                    double min = minValues.getOrDefault(k.getId(), 1.0);
                    // Jika nilai absensi 0 (Sangat Rajin), beri nilai normalisasi sempurna (1.0)
                    nilaiNormalisasi = (nilaiAsli == 0) ? 1.0 : (min / nilaiAsli);
                }

                double bobot = (k.getBobotAHP() == null) ? 0 : k.getBobotAHP();
                totalSkor += (nilaiNormalisasi * bobot);
            }

            // --- LOGIKA 2 TINGKATAN AHP (TERPILIH / GAGAL) ---
            // Asumsi standar passing grade adalah 0.50
            String statusKeputusan = (totalSkor >= 0.50) ? "TERPILIH" : "GAGAL";

            leaderboard.add(RankingResponse.builder()
                    .alternatifId(a.getId())
                    .kode(a.getKode())
                    .nama(a.getNama())
                    .skorAkhir(totalSkor)
                    .status(statusKeputusan)
                    .build());
        }

        leaderboard.sort(Comparator.comparingDouble(RankingResponse::getSkorAkhir).reversed());

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        return leaderboard;
    }
}