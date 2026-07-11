package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Oggetto gioco: da nome e tipo e sa aggiungersi allo studente;
 * polimorfica, non richiede {@code instanceof}).
 */
public interface Item {
    String getNome();

    TipoItem getTipo();

    /**
     * Aggiunge l'item allo studente nel modo proprio della sua categoria
     * (equipaggiamento indossato oppure consumabile messo nello zaino).
     */
    void aggiungiA(Studente studente);
}

