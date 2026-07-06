package it.unicam.cs.mpgc.rpg130525.port;

import java.util.List;

public interface GameView {
    void mostraMessaggio(String messaggio);
    void aggiornaStatoGiocatore(StudenteDto studente);
    void aggiornaStatoProfessore(ProfessoreDto professoreDto);
    void aggiornaMappa(List<StanzaDto> stanze, String posizioneCorrente);
}