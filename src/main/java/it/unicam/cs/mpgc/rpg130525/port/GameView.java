package it.unicam.cs.mpgc.rpg130525.port;

import java.util.List;

/**
 * Contratto di output del gioco: mostra messaggi e aggiorna lo stato di
 * giocatore, professore e mappa, indipendentemente dalla interfaccia utente.
 */
public interface GameView {
    void mostraMessaggio(String messaggio);

    void aggiornaStatoGiocatore(StudenteDto studente);

    void aggiornaStatoProfessore(ProfessoreDto professoreDto);

    void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente);
}