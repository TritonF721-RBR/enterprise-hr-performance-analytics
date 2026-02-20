package com.portofolio.spk_rs.repository;

import com.portofolio.spk_rs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Kita butuh cari user berdasarkan email untuk Login nanti
    Optional<User> findByEmail(String email);
}