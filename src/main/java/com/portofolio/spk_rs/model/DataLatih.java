package com.portofolio.spk_rs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_latih")
public class DataLatih {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kriteria-kriteria (Atribut)
    private String nama;
    private Double nilaiPengalaman; // C1
    private Double nilaiSkill;      // C2
    private Double nilaiAbsensi;    // C3

    // Label (Target yang mau diprediksi)
    // Contoh: "DITERIMA" atau "DITOLAK"
    @Column(nullable = false)
    private String status;
}