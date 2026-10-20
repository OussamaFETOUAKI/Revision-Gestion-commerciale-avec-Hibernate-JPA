package org.example.service;

import org.example.dao.GenericDAO;
import java.io.Serializable;
import java.util.List;

/**
 * Service métier générique fournissant les opérations CRUD standard avec validation des entrées.
 *
 * @param <T>  Type de l'entité gérée
 * @param <ID> Type de l'identifiant
 */
public class CrudService<T, ID extends Serializable> {
    private final GenericDAO<T, ID> dao;

    public CrudService(Class<T> entityClass) {
        this.dao = new GenericDAO<>(entityClass);
    }

    public T getById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant ne peut pas être nul");
        }
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public void create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.create(entity);
    }

    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        return dao.update(entity);
    }

    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.delete(entity);
    }

    public GenericDAO<T, ID> getDao() {
        return dao;
    }
}
