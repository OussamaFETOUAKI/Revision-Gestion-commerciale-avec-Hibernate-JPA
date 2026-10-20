package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Classe utilitaire pour la gestion du cycle de vie de la SessionFactory Hibernate native.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Lecture automatique du fichier hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed. " + ex.getMessage());
            try {
                // Fallback transparent sur H2 si MySQL n'est pas actif localement
                Configuration h2Config = new Configuration();
                h2Config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                h2Config.setProperty("hibernate.connection.url", "jdbc:h2:mem:gestion_commerciale;DB_CLOSE_DELAY=-1;MODE=MySQL");
                h2Config.setProperty("hibernate.connection.username", "sa");
                h2Config.setProperty("hibernate.connection.password", "");
                h2Config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                h2Config.setProperty("hibernate.hbm2ddl.auto", "update");
                h2Config.setProperty("hibernate.show_sql", "true");
                h2Config.setProperty("hibernate.format_sql", "true");
                h2Config.addAnnotatedClass(org.example.entities.Categorie.class);
                h2Config.addAnnotatedClass(org.example.entities.Produit.class);
                h2Config.addAnnotatedClass(org.example.entities.Commande.class);
                h2Config.addAnnotatedClass(org.example.entities.CommandeProduit.class);
                h2Config.addAnnotatedClass(org.example.entities.User.class);
                h2Config.addAnnotatedClass(org.example.entities.Client.class);
                h2Config.addAnnotatedClass(org.example.entities.Fournisseur.class);
                return h2Config.buildSessionFactory();
            } catch (Throwable innerEx) {
                throw new ExceptionInInitializerError(innerEx);
            }
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
