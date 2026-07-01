package it.unicam.cs.mpgc.rpg130525.port;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message, Throwable causa) {
        super(message, causa);
    }
}