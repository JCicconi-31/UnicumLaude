package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Mappa;

/**
 * Contratto per il caricamento della mappa di gioco (stanze, esami, professori,
 * corridoi e propedeuticità) da una sorgente esterna, restituendo solo tipi di
 * dominio: non conosce il file system né alcuna libreria di serializzazione.
 */
public interface LoaderMondo {

    /**
     * Carica e ricostruisce l'intera mappa del gioco.
     *
     * @return la mappa popolata di stanze, corridoi e propedeuticità, mai vuota
     * @throws PersistenceException se la sorgente è illeggibile, modificata o
     * incoerente (es. adiacenze verso stanze inesistenti)
     */
    Mappa caricaMappa() throws PersistenceException;
}