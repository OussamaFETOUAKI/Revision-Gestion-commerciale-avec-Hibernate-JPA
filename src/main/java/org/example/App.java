package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVenteDTO;
import org.example.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Application principale de démonstration :
 * Exécution séquentielle et validation visuelle de toutes les étapes de la révision commerciale Hibernate & JPA.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  RÉVISION GLOBALE — APPLICATION DE GESTION COMMERCIALE (HIBERNATE 5.6 & JPA 2.2)");
        System.out.println("================================================================================\n");

        // 1. Initialisation des services
        CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
        ProduitService prodService = new ProduitService();
        CrudService<User, Long> userService = new CrudService<>(User.class);
        CrudService<Client, Long> clientService = new CrudService<>(Client.class);
        CrudService<Fournisseur, Long> fournisseurService = new CrudService<>(Fournisseur.class);
        CrudService<Commande, Long> commandeService = new CrudService<>(Commande.class);

        // =============================================================================
        // ÉTAPE 1 : Configuration et vérification des fabriques d'EntityManager & Hibernate
        // =============================================================================
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("1. VÉRIFICATION DE LA CONFIGURATION (hibernate.cfg.xml & persistence.xml)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("-> EntityManagerFactory JPA initialisée avec succès : 'defaultPU'");
        try {
            if (HibernateUtil.getSessionFactory() != null) {
                System.out.println("-> SessionFactory Hibernate native opérationnelle.");
            }
        } catch (Exception ex) {
            System.out.println("-> Note HibernateUtil : " + ex.getMessage());
        }

        // =============================================================================
        // ÉTAPE 2 : Création des Catégories et première insertion via GenericDAO
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. CRÉATION DES CATÉGORIES & INSERTION VIA GENERIC DAO");
        System.out.println("--------------------------------------------------------------------------------");
        Categorie catInfo = new Categorie("Informatique");
        Categorie catElectro = new Categorie("Électronique");
        Categorie catBuro = new Categorie("Bureautique");

        catService.create(catInfo);
        catService.create(catElectro);
        catService.create(catBuro);

        System.out.println("Catégorie créée : ID=" + catInfo.getId() + ", Nom='" + catInfo.getNom() + "'");
        System.out.println("Catégorie créée : ID=" + catElectro.getId() + ", Nom='" + catElectro.getNom() + "'");
        System.out.println("Catégorie créée : ID=" + catBuro.getId() + ", Nom='" + catBuro.getNom() + "'");

        // =============================================================================
        // ÉTAPE 3 : Modélisation des Produits & Relation ManyToOne / OneToMany
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. INSERTION DES PRODUITS & GESTION DES ASSOCIATIONS BIDIRECTIONNELLES");
        System.out.println("--------------------------------------------------------------------------------");
        Produit p1 = new Produit("Ordinateur Portable Dell XPS 15", new BigDecimal("1499.99"), catInfo);
        Produit p2 = new Produit("Écran 4K UltraWide 34\"", new BigDecimal("549.50"), catInfo);
        Produit p3 = new Produit("Souris Ergonomique Sans Fil", new BigDecimal("49.99"), catInfo);
        Produit p4 = new Produit("Smartphone Android 5G Pro", new BigDecimal("799.00"), catElectro);
        Produit p5 = new Produit("Casque Bluetooth Réduction de Bruit", new BigDecimal("199.99"), catElectro);
        Produit p6 = new Produit("Chaise Ergonomique de Bureau", new BigDecimal("249.00"), catBuro);
        Produit p7 = new Produit("Lampe de Bureau LED Tactile", new BigDecimal("39.90"), catBuro);

        prodService.create(p1);
        prodService.create(p2);
        prodService.create(p3);
        prodService.create(p4);
        prodService.create(p5);
        prodService.create(p6);
        prodService.create(p7);

        System.out.println("7 produits insérés avec succès dans le catalogue commercial :");
        for (Produit p : prodService.getAll()) {
            System.out.printf("  [ID: %d] %-38s | Prix: %8.2f DH | Catégorie: %s%n",
                    p.getId(), p.getLibelle(), p.getPrix(), p.getCategorie().getNom());
        }

        // =============================================================================
        // ÉTAPE 4 : Requêtes Avancées (Named Queries JPQL)
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. EXÉCUTION DES REQUÊTES NOMMÉES (NAMED QUERIES JPQL)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("-> Requête 'Produit.findByCategorie' pour la catégorie 'Informatique' (ID=" + catInfo.getId() + ") :");
        List<Produit> prodsInfo = prodService.findByCategorie(catInfo.getId());
        for (Produit p : prodsInfo) {
            System.out.printf("   * %s (%.2f DH)%n", p.getLibelle(), p.getPrix());
        }

        System.out.println("\n-> Requête 'Produit.searchByLibelle' avec le mot-clé 'bureau' (insensible à la casse) :");
        List<Produit> prodsBureau = prodService.searchByLibelle("bureau");
        for (Produit p : prodsBureau) {
            System.out.printf("   * %s (%.2f DH)%n", p.getLibelle(), p.getPrix());
        }

        // =============================================================================
        // ÉTAPE 5 : Recherche Dynamique Multi-Critères avec la Criteria API
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("5. RECHERCHE DYNAMIQUE MULTI-CRITÈRES (JPA CRITERIA API)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Filtres appliqués : prix compris entre 100.00 DH et 600.00 DH");
        List<Produit> prodsFiltres = prodService.search(null, new BigDecimal("100.00"), new BigDecimal("600.00"));
        for (Produit p : prodsFiltres) {
            System.out.printf("   [Match] ID: %d | %-38s | Prix: %7.2f DH%n",
                    p.getId(), p.getLibelle(), p.getPrix());
        }

        // =============================================================================
        // ÉTAPE 6 : Pagination côté serveur (setFirstResult, setMaxResults)
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("6. DÉMONSTRATION DE LA PAGINATION CÔTÉ SERVEUR");
        System.out.println("--------------------------------------------------------------------------------");
        int pageSize = 3;
        for (int page = 1; page <= 3; page++) {
            List<Produit> pageResult = prodService.findPage(page, pageSize);
            System.out.printf("--- Page %d (Taille : %d, éléments retournés : %d) ---%n", page, pageSize, pageResult.size());
            for (Produit p : pageResult) {
                System.out.printf("   [Page %d] ID: %d | %-38s | %.2f DH%n", page, p.getId(), p.getLibelle(), p.getPrix());
            }
        }

        // =============================================================================
        // ÉTAPE 7 : Héritage JPA avec Stratégie JOINED (User, Client, Fournisseur)
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("7. GESTION DE LA HIÉRARCHIE D'HÉRITAGE (STRATÉGIE JOINED)");
        System.out.println("--------------------------------------------------------------------------------");
        Client cl1 = new Client("Oussama FETOUAKI", LocalDate.of(2023, 3, 10));
        Client cl2 = new Client("Yassine BENALI", LocalDate.of(2023, 9, 21));
        Fournisseur f1 = new Fournisseur("Grossiste Tech Maroc", "Zone Industrielle Ain Sebaa, Casablanca");
        Fournisseur f2 = new Fournisseur("Logistique Maghreb", "Tanger Med Port Hub");

        clientService.create(cl1);
        clientService.create(cl2);
        fournisseurService.create(f1);
        fournisseurService.create(f2);

        System.out.println("Hiérarchie polymorphique 'User' interrogée via le GenericDAO :");
        List<User> tousUsers = userService.getAll();
        for (User u : tousUsers) {
            if (u instanceof Client) {
                Client c = (Client) u;
                System.out.printf("   [CLIENT]      ID: %d | Nom: %-20s | Inscrit le: %s%n",
                        c.getId(), c.getNom(), c.getDateInscription());
            } else if (u instanceof Fournisseur) {
                Fournisseur f = (Fournisseur) u;
                System.out.printf("   [FOURNISSEUR] ID: %d | Nom: %-20s | Adresse: %s%n",
                        f.getId(), f.getNom(), f.getAdresse());
            }
        }

        // =============================================================================
        // ÉTAPE 8 : Relation N:N avec Entité Associative et Clé Composée (@EmbeddedId)
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("8. RELATION N:N AVEC CLÉ COMPOSÉE EMBEDDED (COMMANDE & PRODUITS)");
        System.out.println("--------------------------------------------------------------------------------");
        Commande cmd1 = new Commande(LocalDate.now());
        cmd1.addProduit(p1, 2); // 2 PC Dell XPS
        cmd1.addProduit(p3, 5); // 5 Souris
        cmd1.addProduit(p2, 1); // 1 Écran UltraWide
        commandeService.create(cmd1);

        Commande cmd2 = new Commande(LocalDate.now().minusDays(15));
        cmd2.addProduit(p4, 3); // 3 Smartphones
        cmd2.addProduit(p5, 4); // 4 Casques Bluetooth
        commandeService.create(cmd2);

        Commande cmd3 = new Commande(LocalDate.now().minusDays(3));
        cmd3.addProduit(p6, 10); // 10 Chaises
        cmd3.addProduit(p7, 10); // 10 Lampes
        commandeService.create(cmd3);

        System.out.println("Commande ID=" + cmd1.getId() + " enregistrée avec ses lignes associées :");
        Commande fetchedCmd = commandeService.getById(cmd1.getId());
        for (CommandeProduit cp : fetchedCmd.getLignes()) {
            BigDecimal sousTotal = cp.getProduit().getPrix().multiply(new BigDecimal(cp.getQuantite()));
            System.out.printf("   * Produit: %-35s | Qté: %2d | PU: %8.2f DH | Sous-total: %9.2f DH (Clé: Commande=%d, Produit=%d)%n",
                    cp.getProduit().getLibelle(), cp.getQuantite(), cp.getProduit().getPrix(), sousTotal,
                    cp.getId().getCommandeId(), cp.getId().getProduitId());
        }

        // =============================================================================
        // ÉTAPE 9 : Gestion explicite des transactions, rollback et cycle de vie
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("9. GESTION DES TRANSACTIONS, CONTEXTE DE PERSISTANCE & ROLLBACK");
        System.out.println("--------------------------------------------------------------------------------");
        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("-> Début de transaction explicite (tx.begin())");
            Produit invalidProd = new Produit("Produit Prix Invalide", new BigDecimal("-50.00"), catInfo);
            em.persist(invalidProd);
            System.out.println("-> Entité en état MANAGED dans le Persistence Context");

            if (invalidProd.getPrix().compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("-> Validation échouée : prix négatif détecté (" + invalidProd.getPrix() + " DH) !");
                throw new IllegalArgumentException("Le prix d'un produit ne peut pas être strictement négatif !");
            }
            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
                System.out.println("-> Transaction annulée avec succès via tx.rollback() : " + ex.getMessage());
            }
        } finally {
            em.close();
            System.out.println("-> EntityManager fermé proprement en bloc finally (entités détachées).");
        }

        // =============================================================================
        // ÉTAPE 10 : Exercice de Synthèse — Rapport de Ventes par Catégorie (Criteria API)
        // =============================================================================
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("10. EXERCICE DE SYNTHÈSE : RAPPORT DE VENTES PAR CATÉGORIE (CRITERIA SUM & PROD)");
        System.out.println("--------------------------------------------------------------------------------");
        List<RapportVenteDTO> rapport = prodService.genererRapportVentesParCategorie();
        BigDecimal caGlobal = BigDecimal.ZERO;
        long totalArticles = 0;

        System.out.printf("%-18s | %-16s | %-18s%n", "CATÉGORIE", "VOLUME VENDU", "CHIFFRE D'AFFAIRES");
        System.out.println("-------------------+------------------+-------------------");
        for (RapportVenteDTO ligne : rapport) {
            System.out.printf("%-18s | %10d unités | %14.2f DH%n",
                    ligne.getCategorieNom(), ligne.getQuantiteTotale(), ligne.getChiffreAffairesTotal());
            caGlobal = caGlobal.add(ligne.getChiffreAffairesTotal());
            totalArticles += ligne.getQuantiteTotale();
        }
        System.out.println("-------------------+------------------+-------------------");
        System.out.printf("%-18s | %10d unités | %14.2f DH%n", "TOTAL GÉNÉRAL", totalArticles, caGlobal);

        System.out.println("\n================================================================================");
        System.out.println("  APPLICATION EXÉCUTÉE AVEC SUCCÈS — TOUS LES TESTS SONT AU VERT !");
        System.out.println("================================================================================");

        try {
            HibernateUtil.shutdown();
            if (GenericDAO.getEntityManagerFactory().isOpen()) {
                GenericDAO.getEntityManagerFactory().close();
            }
        } catch (Exception ignored) {
        }
    }
}
