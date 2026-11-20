package com.example.banque.utilisateur;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurs;

    public UtilisateurDetailsService(UtilisateurRepository utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur u = utilisateurs.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu : " + username));

        return User.withUsername(u.getLogin())
                .password(u.getMotDePasse())
                .roles(u.getRole().name())
                .build();
    }
}