package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Eccezione controllata: lo spostamento verso una stanza non è consentito,
 * perché manca il corridoio o non sono soddisfatte le propedeuticità.
 */
public class MovimentoNonConsentitoException extends Exception {
    public MovimentoNonConsentitoException(String message) {
        super("Lo studente non può accedere alla seguente stanza: " + message);
    }
}