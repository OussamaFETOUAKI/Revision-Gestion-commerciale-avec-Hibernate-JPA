package org.example.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "commande_produit")
public class CommandeProduit implements Serializable {

    @EmbeddedId
    private CommandeProduitPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    public CommandeProduit() {
    }

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.id = new CommandeProduitPK(
                commande != null ? commande.getId() : null,
                produit != null ? produit.getId() : null
        );
    }

    public CommandeProduitPK getId() {
        return id;
    }

    public void setId(CommandeProduitPK id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
        if (this.id == null) {
            this.id = new CommandeProduitPK();
        }
        if (commande != null && commande.getId() != null) {
            this.id.setCommandeId(commande.getId());
        }
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (this.id == null) {
            this.id = new CommandeProduitPK();
        }
        if (produit != null && produit.getId() != null) {
            this.id.setProduitId(produit.getId());
        }
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "CommandeProduit{" +
                "produit=" + (produit != null ? produit.getLibelle() : "null") +
                ", quantite=" + quantite +
                '}';
    }
}
