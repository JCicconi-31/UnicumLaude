package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.StatoGioco;
import it.unicam.cs.mpgc.rpg130525.port.GameView;

public interface FaseEsame {
    boolean esegui(StatoGioco stato, GameView view);
}
