package com.portofolio.spk_rs.dto;

import lombok.Data;

@Data
public class PenilaianRequest {
    private Long alternatifId; // ID Karyawan (misal: 1)
    private Long kriteriaId;   // ID Kriteria (misal: 1)
    private Double nilai;      // Nilainya (misal: 90)
}