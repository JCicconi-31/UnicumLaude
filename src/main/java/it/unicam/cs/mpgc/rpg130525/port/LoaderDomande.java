package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;

import java.util.List;
import java.util.Map;

/**
 * Contratto per il caricamento del database delle domande da una sorgente
 * esterna, restituendo le domande di dominio indicizzate per nome del corso.
 */
public interface LoaderDomande {

    /**
     * Carica le domande della prova scritta, indicizzate per nome del corso.
     *
     * @return mappa {@code nomeCorso -> domande}, con almeno una domanda per corso
     * @throws PersistenceException se la sorgente è illeggibile o malformata
     */
    Map<String, List<Domanda>> caricaDomande() throws PersistenceException;
}