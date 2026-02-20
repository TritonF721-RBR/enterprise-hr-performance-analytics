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
@Table(name = "alternatif")
public class Alternatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kode; // Contoh: A1, A2

    @Column(nullable = false)
    private String nama; // Contoh: Budi Santoso

    private String jabatan; // Contoh: Backend Dev, Frontend Dev

    // Nanti bisa kita tambah kolom 'skorAkhir' atau 'status' kalau butuh
}