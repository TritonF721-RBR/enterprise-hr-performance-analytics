package com.portofolio.spk_rs.repository;

import com.portofolio.spk_rs.model.Alternatif;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternatifRepository extends JpaRepository<Alternatif, Long> {
    boolean existsByKode(String kode);
}