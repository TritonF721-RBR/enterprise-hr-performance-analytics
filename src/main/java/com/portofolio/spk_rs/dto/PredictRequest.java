package com.portofolio.spk_rs.dto;

import lombok.Data;

@Data
public class PredictRequest {
    private String nama;
    private Double nilaiPengalaman;
    private Double nilaiSkill;
    private Double nilaiAbsensi;
}