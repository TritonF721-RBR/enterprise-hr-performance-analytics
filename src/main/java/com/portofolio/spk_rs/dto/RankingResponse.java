package com.portofolio.spk_rs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private int rank;
    private Long alternatifId; // Tambah ID biar gampang dicari
    private String kode;
    private String nama;
    private double skorAkhir;
    private String status; // Tambahan: "DITERIMA" atau "DITOLAK"
}