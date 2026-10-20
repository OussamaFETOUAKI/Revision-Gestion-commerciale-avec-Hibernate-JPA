package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Service métier dédié aux commandes de vente : gestion des lignes associées et attachement des produits au contexte.
 */
public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(Commande.class);
    }

    @Override
    public void create(Commande entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité commande ne peut pas être nulle");
        }
        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (entity.getLignes() != null) {
                for (CommandeProduit cp : entity.getLignes()) {
                    if (cp.getProduit() != null && cp.getProduit().getId() != null) {
                        Produit managedProd = em.find(Produit.class, cp.getProduit().getId());
                        cp.setProduit(managedProd);
                    }
                    cp.setCommande(entity);
                }
            }
            em.persist(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
