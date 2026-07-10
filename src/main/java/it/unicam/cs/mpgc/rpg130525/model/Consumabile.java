package it.unicam.cs.mpgc.rpg130525.model;

public interface Consumabile extends Item {
    void applica(Studente studente);

    @Override
    default void aggiungiA(Studente studente) {
        studente.aggiungiConsumabile(this);
    }
}
