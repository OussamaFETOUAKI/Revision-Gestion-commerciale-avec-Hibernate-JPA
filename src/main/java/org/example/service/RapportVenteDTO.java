package org.example.service;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO représentant le résultat agrégé du rapport de ventes par catégorie (Exercice de synthèse).
 */
public class RapportVenteDTO implements Serializable {
    private String categorieNom;
    private BigDecimal chiffreAffairesTotal;
    private Long quantiteTotale;

    public RapportVenteDTO() {
    }

    public RapportVenteDTO(String categorieNom, Number chiffreAffairesTotal, Number quantiteTotale) {
        this.categorieNom = categorieNom;
        this.chiffreAffairesTotal = chiffreAffairesTotal != null ? new BigDecimal(chiffreAffairesTotal.toString()) : BigDecimal.ZERO;
        this.quantiteTotale = quantiteTotale != null ? quantiteTotale.longValue() : 0L;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    public BigDecimal getChiffreAffairesTotal() {
        return chiffreAffairesTotal;
    }

    public void setChiffreAffairesTotal(BigDecimal chiffreAffairesTotal) {
        this.chiffreAffairesTotal = chiffreAffairesTotal;
    }

    public Long getQuantiteTotale() {
        return quantiteTotale;
    }

    public void setQuantiteTotale(Long quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    @Override
    public String toString() {
        return String.format("Catégorie : %-15s | CA Total : %10.2f DH | Volume vendu : %4d unités",
                categorieNom, chiffreAffairesTotal, quantiteTotale);
    }
}
