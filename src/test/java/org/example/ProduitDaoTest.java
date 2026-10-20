package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVenteDTO;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProduitDaoTest {

    private static CrudService<Categorie, Long> catService;
    private static ProduitService prodService;
    private static CrudService<Client, Long> clientService;
    private static CrudService<Fournisseur, Long> fournisseurService;
    private static CommandeService commandeService;

    @BeforeClass
    public static void init() {
        catService = new CrudService<>(Categorie.class);
        prodService = new ProduitService();
        clientService = new CrudService<>(Client.class);
        fournisseurService = new CrudService<>(Fournisseur.class);
        commandeService = new CommandeService();
    }

    @Test
    public void test01_CreateProduitAvecCategorie() {
        Categorie cat = new Categorie("Papeterie");
        catService.create(cat);
        assertNotNull("L'identifiant de catégorie généré ne doit pas être nul", cat.getId());

        Produit p = new Produit("Stylo à bille", new BigDecimal("1.20"), cat);
        prodService.create(p);
        assertNotNull("L'identifiant du produit généré ne doit pas être nul", p.getId());

        Produit fetched = prodService.getById(p.getId());
        assertNotNull("Le produit récupéré doit exister", fetched);
        assertEquals("Papeterie", fetched.getCategorie().getNom());
        assertEquals(0, new BigDecimal("1.20").compareTo(fetched.getPrix()));
    }

    @Test
    public void test02_NamedQueries() {
        Categorie cat = new Categorie("Informatique");
        catService.create(cat);

        Produit p1 = new Produit("Clavier Mécanique RGB", new BigDecimal("89.90"), cat);
        Produit p2 = new Produit("Souris Ergonomique Sans Fil", new BigDecimal("49.99"), cat);
        prodService.create(p1);
        prodService.create(p2);

        // Test Produit.findAll
        List<Produit> tous = prodService.findAllNamed();
        assertTrue("La liste globale doit contenir au moins 3 produits", tous.size() >= 3);

        // Test Produit.findByCategorie
        List<Produit> prodsInfo = prodService.findByCategorie(cat.getId());
        assertEquals("La catégorie Informatique doit contenir 2 produits", 2, prodsInfo.size());

        // Test Produit.searchByLibelle
        List<Produit> searchClavier = prodService.searchByLibelle("Clavier");
        assertEquals("Une recherche de 'Clavier' doit retourner 1 produit", 1, searchClavier.size());
        assertEquals("Clavier Mécanique RGB", searchClavier.get(0).getLibelle());
    }

    @Test
    public void test03_CriteriaApiRechercheDynamique() {
        // Recherche multi-critères : libellé contenant "Souris" et prix entre 30 et 60
        List<Produit> results = prodService.search("Souris", new BigDecimal("30.00"), new BigDecimal("60.00"));
        assertEquals("La recherche Criteria API doit retourner 1 produit", 1, results.size());
        assertEquals("Souris Ergonomique Sans Fil", results.get(0).getLibelle());

        // Recherche par tranche de prix seule
        List<Produit> pasCher = prodService.search(null, new BigDecimal("1.00"), new BigDecimal("2.00"));
        assertEquals("La recherche du stylo doit retourner 1 produit", 1, pasCher.size());
    }

    @Test
    public void test04_Pagination() {
        // Pagination page 1, taille 2
        List<Produit> page1 = prodService.findPage(1, 2);
        assertEquals("La page 1 doit comporter 2 éléments", 2, page1.size());

        // Pagination page 2, taille 2
        List<Produit> page2 = prodService.findPage(2, 2);
        assertTrue("La page 2 doit comporter au moins 1 élément", page2.size() >= 1);
        assertNotEquals("Les éléments des deux pages doivent être distincts", page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    public void test05_HeritageJoined() {
        Client c = new Client("Amine El Idrissi", LocalDate.of(2023, 1, 15));
        clientService.create(c);
        assertNotNull("L'ID du client ne doit pas être nul", c.getId());

        Fournisseur f = new Fournisseur("Tech Distri SARL", "123 Bd Zerktouni, Casablanca");
        fournisseurService.create(f);
        assertNotNull("L'ID du fournisseur ne doit pas être nul", f.getId());

        Client fetchedClient = clientService.getById(c.getId());
        assertEquals("Amine El Idrissi", fetchedClient.getNom());
        assertEquals(LocalDate.of(2023, 1, 15), fetchedClient.getDateInscription());

        Fournisseur fetchedFournisseur = fournisseurService.getById(f.getId());
        assertEquals("Tech Distri SARL", fetchedFournisseur.getNom());
        assertEquals("123 Bd Zerktouni, Casablanca", fetchedFournisseur.getAdresse());
    }

    @Test
    public void test06_CommandeProduitCleComposee() {
        Categorie cat = new Categorie("Maroquinerie");
        catService.create(cat);

        Produit sac = new Produit("Sacoche Ordinateur 15 pouces", new BigDecimal("120.00"), cat);
        Produit tapis = new Produit("Tapis de Souris XXL", new BigDecimal("25.00"), cat);
        prodService.create(sac);
        prodService.create(tapis);

        Commande cmd = new Commande(LocalDate.now());
        cmd.addProduit(sac, 2);
        cmd.addProduit(tapis, 4);

        commandeService.create(cmd);
        assertNotNull("L'ID de commande généré ne doit pas être nul", cmd.getId());

        Commande fetchedCmd = commandeService.getById(cmd.getId());
        assertEquals("La commande doit comporter 2 lignes d'articles", 2, fetchedCmd.getLignes().size());

        CommandeProduit ligne1 = fetchedCmd.getLignes().get(0);
        assertNotNull("La clé primaire composite ne doit pas être nulle", ligne1.getId());
        assertEquals(fetchedCmd.getId(), ligne1.getId().getCommandeId());
    }

    @Test
    public void test07_GestionTransactionsRollback() {
        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        Long tempId = null;

        try {
            tx.begin();
            Categorie cat = new Categorie("Catégorie Rollback Test");
            em.persist(cat);
            tempId = cat.getId();

            // Déclenchement intentionnel d'une exception
            throw new IllegalStateException("Simulation d'erreur métier provoquant le rollback");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }

        // Vérification que la catégorie n'a pas été commitée
        if (tempId != null) {
            Categorie nonExistant = catService.getById(tempId);
            assertNull("La transaction annulée ne doit pas persister l'entité", nonExistant);
        }
    }

    @Test
    public void test08_ExerciceSyntheseRapportVentes() {
        List<RapportVenteDTO> rapport = prodService.genererRapportVentesParCategorie();
        assertNotNull("Le rapport ne doit pas être nul", rapport);
        assertFalse("Le rapport de ventes doit contenir des entrées agrégées", rapport.isEmpty());

        for (RapportVenteDTO ligne : rapport) {
            assertNotNull(ligne.getCategorieNom());
            assertTrue("Le CA doit être positif", ligne.getChiffreAffairesTotal().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Le volume vendu doit être supérieur ou égal à zéro", ligne.getQuantiteTotale() >= 0);
        }
    }
}
