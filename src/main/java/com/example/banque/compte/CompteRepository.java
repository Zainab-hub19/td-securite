package com.example.banque.compte;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    List<Compte> findByTitulaireUtilisateurLogin(String login);
    List<Compte> findByAgenceId(Long agenceId);
}
