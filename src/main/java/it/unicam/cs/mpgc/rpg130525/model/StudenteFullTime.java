package it.unicam.cs.mpgc.rpg130525.model;

public class StudenteFullTime implements CareerStrategy{

    @Override
    public int modificatoreHpMax() {
        return +20;
    }

    @Override
    public int modificatoreMoneteIniziali() {
        return 0;
    }
}
