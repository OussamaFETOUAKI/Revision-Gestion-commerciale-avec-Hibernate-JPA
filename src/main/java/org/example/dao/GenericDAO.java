package org.example.dao;

import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.List;

/**
 * DAO générique réutilisable fondé sur l'API JPA standard (EntityManager, EntityTransaction).
 * Assure une gestion robuste des sessions et des fermetures systématiques en bloc finally.
 *
 * @param <T>  Type de l'entité
 * @param <ID> Type de la clé primaire (Serializable)
 */
public class GenericDAO<T, ID extends Serializable> {
    private final Class<T> entityClass;
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU");

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T find(ID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    public void create(T entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Si l'entité est une Commande, attacher les produits détachés au contexte de persistance
            if (entity instanceof Commande) {
                Commande cmd = (Commande) entity;
                if (cmd.getLignes() != null) {
                    for (CommandeProduit cp : cmd.getLignes()) {
                        if (cp.getProduit() != null && cp.getProduit().getId() != null) {
                            cp.setProduit(em.find(Produit.class, cp.getProduit().getId()));
                        }
                        cp.setCommande(cmd);
                    }
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

    public T update(T entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(T entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T attached = em.contains(entity) ? entity : em.merge(entity);
            em.remove(attached);
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

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}
