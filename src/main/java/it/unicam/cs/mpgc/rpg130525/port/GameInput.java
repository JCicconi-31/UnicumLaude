package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Domanda;

import java.util.List;

/**
 * Contratto di input del gioco: raccoglie le scelte del giocatore (risposte,
 * selezioni da menù, testo) indipendentemente dalla UI (console o grafica).
 */
public interface GameInput {

    /**
     * Pone la domanda al giocatore e ne raccoglie la risposta.
     *
     * @return l'indice (in base 0) dell'opzione scelta, compreso tra 0 e
     * {@code domanda.getOpzioni().size() - 1}
     */
    int chiediRisposta(Domanda domanda);

    /**
     * Propone al giocatore un elenco di opzioni con un titolo.
     *
     * @return l'indice (in base 0) dell'opzione scelta, compreso tra 0 e
     * {@code opzioni.size() - 1}
     */
    int scegli(String titolo, List<String> opzioni);

    /**
     * Chiede al giocatore un testo libero.
     *
     * @return il testo inserito, mai nullo né vuoto
     */
    String chiediTesto(String prompt);
}
