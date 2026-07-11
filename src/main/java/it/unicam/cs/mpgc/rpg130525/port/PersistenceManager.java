package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;

/**
 * Contratto per il salvataggio e il caricamento dello stato di gioco,
 * indipendente dalla tecnologia di persistenza usata.
 */
public interface PersistenceManager {

    /**
     * Salva lo stato di gioco, sovrascrivendo l'eventuale salvataggio precedente.
     *
     * @throws PersistenceException se il salvataggio fallisce
     */
    void salva(StatoGioco stato);

    /**
     * Ricarica l'ultimo stato di gioco salvato.
     *
     * @return lo stato ripristinato, con stanze ed esami riagganciati alla mappa
     * @throws PersistenceException se non esiste un salvataggio o la lettura fallisce
     */
    StatoGioco carica();

    /**
     * @return true se esiste un salvataggio da cui riprendere la partita
     */
    boolean esisteSalvataggio();
}