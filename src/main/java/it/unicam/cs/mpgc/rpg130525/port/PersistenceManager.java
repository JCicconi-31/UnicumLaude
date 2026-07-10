package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;

public interface PersistenceManager {
    void salva(StatoGioco stato);

    StatoGioco carica();

    boolean esisteSalvataggio();
}