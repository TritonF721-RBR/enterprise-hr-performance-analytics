package com.portofolio.spk_rs.service;

import com.portofolio.spk_rs.model.Alternatif;
import com.portofolio.spk_rs.repository.AlternatifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlternatifService {

    private final AlternatifRepository repository;

    public List<Alternatif> getAllAlternatif() {
        return repository.findAll();
    }

    public Alternatif createAlternatif(Alternatif alternatif) {
        if (repository.existsByKode(alternatif.getKode())) {
            throw new RuntimeException("Kode alternatif " + alternatif.getKode() + " sudah ada!");
        }
        return repository.save(alternatif);
    }

    public Alternatif updateAlternatif(Long id, Alternatif alternatifBaru) {
        Alternatif alternatifLama = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alternatif tidak ditemukan"));

        alternatifLama.setNama(alternatifBaru.getNama());
        alternatifLama.setJabatan(alternatifBaru.getJabatan());
        // Kode biasanya tidak diubah agar konsisten

        return repository.save(alternatifLama);
    }

    public void deleteAlternatif(Long id) {
        repository.deleteById(id);
    }
}