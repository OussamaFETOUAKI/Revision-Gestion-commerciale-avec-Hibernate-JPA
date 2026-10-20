package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service métier dédié aux produits : Named Queries, Criteria API, pagination et agrégation.
 */
public class ProduitService extends CrudService<Produit, Long> {

    public ProduitService() {
        super(Produit.class);
    }

    private EntityManagerFactory getEmf() {
        return GenericDAO.getEntityManagerFactory();
    }

    /**
     * Recherche de tous les produits via la Named Query 'Produit.findAll'
     */
    public List<Produit> findAllNamed() {
        EntityManager em = getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.findAll", Produit.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche des produits par catégorie via la Named Query 'Produit.findByCategorie'
     */
    public List<Produit> findByCategorie(Long catId) {
        EntityManager em = getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.findByCategorie", Produit.class)
                    .setParameter("catId", catId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche des produits par libellé via la Named Query 'Produit.searchByLibelle'
     */
    public List<Produit> searchByLibelle(String term) {
        EntityManager em = getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                    .setParameter("term", term)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche multi-critères dynamique utilisant l'API Criteria (Step 8)
     */
    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        EntityManager em = getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.toLowerCase() + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.ge(root.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.le(root.get("prix"), maxPrix));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Pagination serveur avec JPQL via setFirstResult et setMaxResults (Step 9)
     */
    public List<Produit> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        EntityManager em = getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                    .setFirstResult((pageNumber - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Exercice de synthèse (Step 12) :
     * Rapport de ventes par catégorie calculant le chiffre d'affaires total (SUM(prix * quantite))
     * à l'aide des fonctions CriteriaBuilder.sum et CriteriaBuilder.prod.
     */
    public List<RapportVenteDTO> genererRapportVentesParCategorie() {
        EntityManager em = getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<RapportVenteDTO> cq = cb.createQuery(RapportVenteDTO.class);
            Root<CommandeProduit> cpRoot = cq.from(CommandeProduit.class);

            Join<CommandeProduit, Produit> prodJoin = cpRoot.join("produit");
            Join<Produit, Categorie> catJoin = prodJoin.join("categorie");

            Expression<Number> chiffreAffaireLigne = cb.prod(prodJoin.get("prix"), cpRoot.get("quantite"));
            Expression<Number> totalCA = cb.sum(chiffreAffaireLigne);
            Expression<Number> totalQuantite = cb.sum(cpRoot.get("quantite"));

            cq.select(cb.construct(
                    RapportVenteDTO.class,
                    catJoin.get("nom"),
                    totalCA,
                    totalQuantite
            ));

            cq.groupBy(catJoin.get("id"), catJoin.get("nom"));
            cq.orderBy(cb.desc(totalCA));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
}
