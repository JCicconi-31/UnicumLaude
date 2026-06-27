package it.unicam.cs.mpgc.rpg130525.model;

public class PrerequisitiNonRispettatiException extends Exception {
    public PrerequisitiNonRispettatiException(String message) {
        super("Lo studente non può accedere alla seguente stanza: " + message);
    }
}