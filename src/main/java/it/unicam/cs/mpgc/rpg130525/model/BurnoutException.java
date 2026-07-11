package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Eccezione: lo studente ha esaurito la salute mentale, il che
 * determina la fine della partita.
 */
public class BurnoutException extends RuntimeException {
    public BurnoutException(String message) {
        super(message);
    }
}
