package com.example.banque.compte;

import com.example.banque.utilisateur.*;
import com.example.banque.utilisateur.out.UtilisateurCourant;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class CompteService {

    private final CompteRepository comptes;
    private final ClientRepository clients;
    private final ConseillerRepository conseillers;
    private final UtilisateurCourant utilisateurCourant;

    public CompteService(CompteRepository comptes, ClientRepository clients,
                         ConseillerRepository conseillers, UtilisateurCourant utilisateurCourant) {
        this.comptes = comptes;
        this.clients = clients;
        this.conseillers = conseillers;
        this.utilisateurCourant = utilisateurCourant;
    }

    public List<CompteDto> lister() {
        Utilisateur u = courant();
        if (u.getRole() == Role.CLIENT) {
            return comptes.findByTitulaireUtilisateurLogin(u.getLogin())
                    .stream().map(CompteDto::de).toList();
        }
        Conseiller conseiller = conseillers.findByUtilisateurLogin(u.getLogin())
                .orElseThrow(() -> new AccessDeniedException("Profil conseiller introuvable"));
        return comptes.findByAgenceId(conseiller.getAgence().getId())
                .stream().map(CompteDto::de).toList();
    }

    public CompteDto trouver(Long id) {
        Compte compte = comptes.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Compte " + id + " introuvable"));
        verifierLecture(compte);
        return CompteDto.de(compte);
    }

    public void verifierLecture(Compte compte) {
        Utilisateur u = courant();
        if (u.getRole() == Role.CLIENT) {
            if (!compte.getTitulaire().getUtilisateur().getId().equals(u.getId())) {
                throw new AccessDeniedException("Accès refusé à ce compte");
            }
        } else if (u.getRole() == Role.CONSEILLER) {
            Conseiller conseiller = conseillers.findByUtilisateurLogin(u.getLogin())
                    .orElseThrow(() -> new AccessDeniedException("Profil conseiller introuvable"));
            if (!compte.getAgence().getId().equals(conseiller.getAgence().getId())) {
                throw new AccessDeniedException("Ce compte dépend d'une autre agence");
            }
        }
    }

    public void verifierTitulaire(Compte compte) {
        Utilisateur u = courant();
        if (u.getRole() != Role.CLIENT || !compte.getTitulaire().getUtilisateur().getId().equals(u.getId())) {
            throw new AccessDeniedException("Seul le titulaire du compte peut émettre un virement");
        }
    }

    private Utilisateur courant() {
        return utilisateurCourant.get()
                .orElseThrow(() -> new AccessDeniedException("Aucun utilisateur connecté"));
    }
}
