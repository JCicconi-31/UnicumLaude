package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Carriera Full-time: bonus agli HP massimi, nessun bonus alle monete iniziali.
 */
public class StudenteFullTime implements CareerStrategy {

    @Override
    public int modificatoreHpMax() {
        return +20;
    }

    @Override
    public int modificatoreMoneteIniziali() {
        return 0;
    }

    @Override
    public String getNome() {
        return "Studente Full-time";
    }
}
