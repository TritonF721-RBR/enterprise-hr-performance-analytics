package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.dto.RankingResponse;
import com.portofolio.spk_rs.repository.KriteriaRepository;
import com.portofolio.spk_rs.repository.PenilaianRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class C45ServiceTest {

    // 1. Siapkan 3 "Database Palsu" sesuai permintaan C45Service
    @Mock
    private RankingService rankingService;

    @Mock
    private PenilaianRepository penilaianRepository;

    @Mock
    private KriteriaRepository kriteriaRepository;

    // 2. Suntikkan database palsu tersebut ke mesin utama C4.5
    @InjectMocks
    private C45Service c45Service;

    @Test
    void analyzeExistingData_SkenarioDataKosong() {
        // ==========================================
        // A (ARRANGE) - Siapkan Kondisi Kosong
        // ==========================================
        // Kita paksa RankingService (AHP) untuk mengembalikan list kosong
        when(rankingService.hitungPerankingan()).thenReturn(new ArrayList<>());
        when(penilaianRepository.findAll()).thenReturn(new ArrayList<>());
        when(kriteriaRepository.findAll()).thenReturn(new ArrayList<>());

        // ==========================================
        // A (ACT) - Jalankan Mesin C4.5
        // ==========================================
        C45Service.C45Result hasil = c45Service.analyzeExistingData();

        // ==========================================
        // A (ASSERT) - Buktikan Logika Baris 47-50 Berjalan Benar
        // ==========================================
        assertNotNull(hasil, "Hasil tidak boleh null");

        // Memastikan mesin mengembalikan teks peringatan yang mengandung kata "Belum ada data"
        assertTrue(hasil.getRuleGenerated().contains("Belum ada data"),
                "Pesan error salah! Seharusnya sistem menolak karena data kosong.");
    }

    @Test
    void analyzeExistingData_SkenarioPrediksiAkurat() {
        // ==========================================
        // 1. ARRANGE - Siapkan Data Palsu (Mock)
        // ==========================================
        // Buat 2 Kriteria: C1 (Kinerja - BENEFIT), C2 (Absensi - COST)
        com.portofolio.spk_rs.model.Kriteria k1 = new com.portofolio.spk_rs.model.Kriteria();
        k1.setKode("C1");
        k1.setJenis(com.portofolio.spk_rs.model.JenisKriteria.BENEFIT);

        com.portofolio.spk_rs.model.Kriteria k2 = new com.portofolio.spk_rs.model.Kriteria();
        k2.setKode("C2");
        k2.setJenis(com.portofolio.spk_rs.model.JenisKriteria.COST);

        java.util.List<com.portofolio.spk_rs.model.Kriteria> listKriteria = java.util.List.of(k1, k2);

        // Buat 2 Alternatif (Karyawan)
        com.portofolio.spk_rs.model.Alternatif a1 = new com.portofolio.spk_rs.model.Alternatif();
        a1.setId(1L); a1.setNama("Budi");

        com.portofolio.spk_rs.model.Alternatif a2 = new com.portofolio.spk_rs.model.Alternatif();
        a2.setId(2L); a2.setNama("Andi");

        // Buat Hasil AHP dari RankingService (Budi Lulus, Andi Gagal)
        RankingResponse r1 = new RankingResponse();
        r1.setAlternatifId(1L); r1.setNama("Budi"); r1.setStatus("TERPILIH");

        RankingResponse r2 = new RankingResponse();
        r2.setAlternatifId(2L); r2.setNama("Andi"); r2.setStatus("TIDAK TERPILIH");

        java.util.List<RankingResponse> listRanking = java.util.List.of(r1, r2);

        // Buat Nilai mentah
        // Budi: Kinerja 80 (Bagus), Absensi 10 (Sedikit)
        com.portofolio.spk_rs.model.Penilaian p1 = new com.portofolio.spk_rs.model.Penilaian(); p1.setAlternatif(a1); p1.setKriteria(k1); p1.setNilai(80.0);
        com.portofolio.spk_rs.model.Penilaian p2 = new com.portofolio.spk_rs.model.Penilaian(); p2.setAlternatif(a1); p2.setKriteria(k2); p2.setNilai(10.0);

        // Andi: Kinerja 60 (Jelek), Absensi 20 (Banyak)
        com.portofolio.spk_rs.model.Penilaian p3 = new com.portofolio.spk_rs.model.Penilaian(); p3.setAlternatif(a2); p3.setKriteria(k1); p3.setNilai(60.0);
        com.portofolio.spk_rs.model.Penilaian p4 = new com.portofolio.spk_rs.model.Penilaian(); p4.setAlternatif(a2); p4.setKriteria(k2); p4.setNilai(20.0);

        java.util.List<com.portofolio.spk_rs.model.Penilaian> listPenilaian = java.util.List.of(p1, p2, p3, p4);

        // Ajari Mockito
        when(rankingService.hitungPerankingan()).thenReturn(listRanking);
        when(kriteriaRepository.findAll()).thenReturn(listKriteria);
        when(penilaianRepository.findAll()).thenReturn(listPenilaian);

        // ==========================================
        // 2. ACT - Jalankan Mesin C4.5
        // ==========================================
        C45Service.C45Result hasil = c45Service.analyzeExistingData();

        // ==========================================
        // 3. ASSERT - Buktikan Mesin Menebak dengan Benar
        // ==========================================
        assertNotNull(hasil);
        assertEquals(2, hasil.getDetails().size(), "Harus memproses 2 karyawan");

        C45Service.C45Prediction tebakanBudi = hasil.getDetails().get(0);
        C45Service.C45Prediction tebakanAndi = hasil.getDetails().get(1);

        // Budi harusnya tembus jadi "TERBAIK"
        assertEquals("Budi", tebakanBudi.getNama());
        assertEquals("TERBAIK", tebakanBudi.getPrediksiC45(), "Budi seharusnya dapat status TERBAIK karena nilainya di atas rata-rata kelompok Terpilih");

        // Andi harusnya jatuh ke "GAGAL"
        assertEquals("Andi", tebakanAndi.getNama());
        assertEquals("GAGAL", tebakanAndi.getPrediksiC45(), "Andi seharusnya GAGAL karena nilainya di bawah standar");
        assertTrue(tebakanAndi.getKeterangan().contains("Evaluasi:"), "Sistem harus memberi alasan kegagalan Andi");
    }
}