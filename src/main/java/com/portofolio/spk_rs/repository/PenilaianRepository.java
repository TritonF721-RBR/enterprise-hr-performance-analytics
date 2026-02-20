package com.portofolio.spk_rs.repository;

import com.portofolio.spk_rs.model.Alternatif;
import com.portofolio.spk_rs.model.Kriteria;
import com.portofolio.spk_rs.model.Penilaian;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PenilaianRepository extends JpaRepository<Penilaian, Long> {
    // Cari nilai berdasarkan orang & kriteria tertentu (biar gak dobel input)
    Optional<Penilaian> findByAlternatifAndKriteria(Alternatif alternatif, Kriteria kriteria);

    // Cari semua nilai milik si Budi (misalnya)
    List<Penilaian> findByAlternatif(Alternatif alternatif);
}