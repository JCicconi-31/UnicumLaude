package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

/**
 * Contratto di una fase d'esame componibile: restituisce {@code true} se la fase
 * è superata, {@code false} altrimenti.
 */
public interface FaseEsame {

    /**
     * Esegue la fase nell'aula d'esame in cui si trova il giocatore.
     *
     * @return true se la fase è superata, false se è fallita
     * @throws it.unicam.cs.mpgc.rpg130525.model.BurnoutException se durante la
     * fase lo studente esaurisce la salute mentale.
     */
    boolean esegui(StatoGioco stato, GameView view);
}
