package it.unicam.cs.mpgc.rpg130525.port;

public interface GameView {
    void mostraMessaggio(String messaggio);
    void aggiornaStatoGiocatore(StudenteDto studente);
}