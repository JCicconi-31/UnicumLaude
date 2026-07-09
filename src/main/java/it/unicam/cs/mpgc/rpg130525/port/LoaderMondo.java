package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Mappa;

public interface LoaderMondo {
    Mappa caricaMappa() throws PersistenceException;
}