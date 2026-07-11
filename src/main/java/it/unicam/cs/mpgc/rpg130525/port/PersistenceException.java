package it.unicam.cs.mpgc.rpg130525.port;

/**
 * Eccezione non controllata che segnala un fallimento di persistenza (I/O o
 * serializzazione), incapsulando la causa originaria.
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message, Throwable causa) {
        super(message, causa);
    }
}