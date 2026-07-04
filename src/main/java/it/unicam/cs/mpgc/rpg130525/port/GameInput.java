package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;

import java.util.List;

public interface GameInput {

    int chiediRisposta(Domanda domanda);

    int scegli(String titolo, List<String> opzioni);

    String chiediTesto(String prompt);
}
