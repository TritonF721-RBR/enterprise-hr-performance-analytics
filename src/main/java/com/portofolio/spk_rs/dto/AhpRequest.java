package com.portofolio.spk_rs.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AhpRequest {
    // Format: { "ID_Kriteria_1": { "ID_Kriteria_2": Nilai_Banding } }
    // Contoh: { "1": { "2": 3.0, "3": 5.0 }, "2": { "3": 2.0 } }
    private Map<Long, Map<Long, Double>> perbandingan;
}