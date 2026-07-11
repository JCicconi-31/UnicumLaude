package it.unicam.cs.mpgc.rpg130525.engine;

import it.unicam.cs.mpgc.rpg130525.model.Professore;
import it.unicam.cs.mpgc.rpg130525.model.Studente;

/**
 * Centralizza le formule di danno del gioco: quello inflitto dallo studente e
 * quello subìto in base alla difficoltà del professore e alla resilienza.
 */
public class CalcolatoreDanno {
    private static final int DANNO_MINIMO = 1;

    /**
     * Danno che lo studente infligge al professore rispondendo.
     *
     * @return un valore positivo pari all'intelligenza effettiva dello studente
     */
    public int dannoInflitto(Studente studente) {
        if (studente == null)
            throw new IllegalArgumentException("studente passato null");
        return studente.getIntelligenzaEffettiva();
    }

    /**
     * Danno che lo studente subisce dall'incalzare del professore.
     *
     * @return almeno {@value DANNO_MINIMO} se lo studente è ancora in gioco,
     * 0 se ha già esaurito la salute mentale
     */
    public int dannoSubito(Professore professore, Studente studente) {
        if (professore == null || studente == null)
            throw new IllegalArgumentException("impossibile calcolare danno subito su una o più entity null");
        if (studente.getSaluteMentale() > 0) {
            return Math.max(DANNO_MINIMO, professore.getDifficolta() - studente.getResilienzaEffettiva());
        }
        return 0;
    }
}