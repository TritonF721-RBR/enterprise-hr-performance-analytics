package com.portofolio.spk_rs.repository;

import com.portofolio.spk_rs.model.DataLatih;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataLatihRepository extends JpaRepository<DataLatih, Long> {
    // Nanti kita butuh ambil semua data untuk dihitung Entropi-nya
}