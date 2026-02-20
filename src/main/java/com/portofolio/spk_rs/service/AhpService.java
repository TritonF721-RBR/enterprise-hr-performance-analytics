package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.dto.AhpRequest;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AhpService {

    private final KriteriaRepository kriteriaRepository;

    @Transactional
    public List<Kriteria> hitungDanSimpanBobot(AhpRequest request) {
        List<Kriteria> listKriteria = kriteriaRepository.findAll();
        int n = listKriteria.size();

        // 1. Siapkan Matriks Perbandingan (n x n)
        double[][] matriks = new double[n][n];
        Map<Long, Integer> idToIndex = new HashMap<>();

        // Map ID Kriteria ke Index Array (0, 1, 2...)
        for (int i = 0; i < n; i++) {
            idToIndex.put(listKriteria.get(i).getId(), i);
            matriks[i][i] = 1.0; // Diagonal pasti 1
        }

        // 2. Isi Matriks dari Input User
        var input = request.getPerbandingan();
        if (input != null) {
            input.forEach((id1, mapLawan) -> {
                Integer idx1 = idToIndex.get(Long.valueOf(id1));
                if (idx1 != null) {
                    mapLawan.forEach((id2, nilai) -> {
                        Integer idx2 = idToIndex.get(Long.valueOf(id2));
                        if (idx2 != null) {
                            matriks[idx1][idx2] = nilai;
                            matriks[idx2][idx1] = 1.0 / nilai; // Nilai kebalikan otomatis
                        }
                    });
                }
            });
        }

        // 3. Hitung Jumlah Kolom
        double[] jumlahKolom = new double[n];
        for (int j = 0; j < n; j++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += matriks[i][j];
            }
            jumlahKolom[j] = sum;
        }

        // 4. Normalisasi & Hitung Bobot (Rata-rata Baris)
        for (int i = 0; i < n; i++) {
            double jumlahBarisNormalisasi = 0;
            for (int j = 0; j < n; j++) {
                double nilaiNormal = matriks[i][j] / jumlahKolom[j];
                jumlahBarisNormalisasi += nilaiNormal;
            }
            double bobot = jumlahBarisNormalisasi / n;

            // Simpan bobot ke object Kriteria
            listKriteria.get(i).setBobotAHP(bobot);
        }

        // 5. Update ke Database
        return kriteriaRepository.saveAll(listKriteria);
    }
}