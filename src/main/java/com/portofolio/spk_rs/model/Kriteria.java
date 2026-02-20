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
@Table(name = "kriteria")
public class Kriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kode; // Contoh: C1, C2, C3

    @Column(nullable = false)
    private String nama; // Contoh: Pengalaman, Skill Java

    @Enumerated(EnumType.STRING)
    private JenisKriteria jenis; // BENEFIT atau COST

    private Double bobotAHP; // Nilai bobot hasil hitungan AHP nanti
}