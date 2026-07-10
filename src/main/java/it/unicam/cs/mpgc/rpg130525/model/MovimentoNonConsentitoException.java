package it.unicam.cs.mpgc.rpg130525.model;

public class MovimentoNonConsentitoException extends Exception {
    public MovimentoNonConsentitoException(String message) {
        super("Lo studente non può accedere alla seguente stanza: " + message);
    }
}