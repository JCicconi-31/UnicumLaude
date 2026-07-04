package it.unicam.cs.mpgc.rpg130525.model;

public interface CareerStrategy {
    int modificatoreHpMax();
    int modificatoreMoneteIniziali();

    String getNome();

    default String getDescrizione() {
        return String.format("HP max %+d, monete iniziali %+d",
                modificatoreHpMax(), modificatoreMoneteIniziali());
    }
}
