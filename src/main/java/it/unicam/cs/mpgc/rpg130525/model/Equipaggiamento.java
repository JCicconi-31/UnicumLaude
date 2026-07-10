package it.unicam.cs.mpgc.rpg130525.model;

import java.util.Map;

public interface Equipaggiamento extends Item {
    Map<Stat, Integer> getModificatori();

    @Override
    default void aggiungiA(Studente studente) {
        studente.equipaggiaItem(this);
    }
}
