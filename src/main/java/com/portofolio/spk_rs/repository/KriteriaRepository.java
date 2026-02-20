package com.portofolio.spk_rs.repository;

import com.portofolio.spk_rs.model.Kriteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KriteriaRepository extends JpaRepository<Kriteria, Long> {
    // Nanti kita butuh cek biar kode kriteria gak dobel
    boolean existsByKode(String kode);
}