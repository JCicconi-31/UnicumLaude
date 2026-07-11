package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Item a uso singolo che applica un effetto immediato allo studente.
 */
public interface Consumabile extends Item {

    /**
     * Applica allo studente l'effetto una tantum dell'item (es. caffè).
     */
    void applica(Studente studente);

    @Override
    default void aggiungiA(Studente studente) {
        studente.aggiungiConsumabile(this);
    }
}
