package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;

import java.util.List;
import java.util.Map;

public interface LoaderDomande {
    Map<String, List<Domanda>> caricaDomande() throws PersistenceException;
}