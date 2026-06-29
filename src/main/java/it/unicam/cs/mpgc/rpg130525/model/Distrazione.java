package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Nemico minore di tipo "Distrazione": ciò che sottrae concentrazione allo
 * studente nelle aule studio e nei corridoi (sez. 1.5.1 del documento di analisi).
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public class Distrazione extends NemicoMinore {
    public Distrazione(int dannoStress) {
        super("Distrazione", dannoStress);
    }
}
