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
@Table(name = "penilaian", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"alternatif_id", "kriteria_id"}) // Satu orang cuma boleh punya 1 nilai per kriteria
})
public class Penilaian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alternatif_id", nullable = false)
    private Alternatif alternatif;

    @ManyToOne
    @JoinColumn(name = "kriteria_id", nullable = false)
    private Kriteria kriteria;

    @Column(nullable = false)
    private Double nilai; // Contoh: 90.0, 85.5
}