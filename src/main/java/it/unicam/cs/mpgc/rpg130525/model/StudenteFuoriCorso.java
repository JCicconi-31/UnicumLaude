package it.unicam.cs.mpgc.rpg130525.model;

public class StudenteFuoriCorso implements CareerStrategy{

    @Override
    public int modificatoreHpMax() {
        return -10;
    }

    @Override
    public int modificatoreMoneteIniziali() {
        return -10;
    }
}
