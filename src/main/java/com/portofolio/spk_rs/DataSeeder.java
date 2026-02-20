package com.portofolio.spk_rs;

import com.portofolio.spk_rs.repository.DataLatihRepository;
import com.portofolio.spk_rs.auth.AuthenticationService;
import com.portofolio.spk_rs.auth.RegisterRequest;
import com.portofolio.spk_rs.model.DataLatih;
import com.portofolio.spk_rs.model.UserRole;
import com.portofolio.spk_rs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AuthenticationService service;
    private final UserRepository repository;

    @Autowired // Tambahkan ini di atas constructor atau field injection jika perlu, atau biarkan Lombok
    private final DataLatihRepository dataLatihRepository;

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah sudah ada user di database?
        if (repository.count() == 0) {
            // Kalau kosong, buatkan 1 Admin Default
            var admin = RegisterRequest.builder()
                    .fullName("Administrator Sistem")
                    .email("admin@rs.com")
                    .password("admin123") // Password Admin
                    .build();

            service.register(admin);

            // Ubah role-nya jadi ADMIN manual (karena default register adalah USER)
            var savedUser = repository.findByEmail("admin@rs.com").get();
            savedUser.setRole(UserRole.ADMIN);
            repository.save(savedUser);

            System.out.println("✅ ADMIN DEFAULT BERHASIL DIBUAT: admin@rs.com / admin123");

            // Cek Data Latih, kalau kosong kita isi 5 data contoh
            if (dataLatihRepository.count() == 0) {
                List<DataLatih> data = List.of(
                        DataLatih.builder().nama("D1").nilaiPengalaman(3.0).nilaiSkill(80.0).nilaiAbsensi(0.0).status("DITERIMA").build(),
                        DataLatih.builder().nama("D2").nilaiPengalaman(1.0).nilaiSkill(50.0).nilaiAbsensi(5.0).status("DITOLAK").build(),
                        DataLatih.builder().nama("D3").nilaiPengalaman(4.0).nilaiSkill(90.0).nilaiAbsensi(1.0).status("DITERIMA").build(),
                        DataLatih.builder().nama("D4").nilaiPengalaman(0.5).nilaiSkill(40.0).nilaiAbsensi(10.0).status("DITOLAK").build(),
                        DataLatih.builder().nama("D5").nilaiPengalaman(2.0).nilaiSkill(75.0).nilaiAbsensi(2.0).status("DITERIMA").build()
                );
                dataLatihRepository.saveAll(data);
                System.out.println("✅ DATA LATIH BERHASIL DIGENERATE (5 DATA)");
            }
        }
    }
}