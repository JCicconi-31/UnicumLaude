package it.unicam.cs.mpgc.rpg130525.model;

import java.util.Map;

/**
 * Item equipaggiabile che fornisce modificatori permanenti alle statistiche.
 */
public interface Equipaggiamento extends Item {

    /**
     * @return i modificatori alle statistiche forniti dall'item, mai nulli
     */
    Map<Stat, Integer> getModificatori();

    @Override
    default void aggiungiA(Studente studente) {
        studente.equipaggiaItem(this);
    }
}
