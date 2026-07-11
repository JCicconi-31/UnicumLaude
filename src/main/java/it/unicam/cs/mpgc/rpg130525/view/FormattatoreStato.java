package it.unicam.cs.mpgc.rpg130525.view;

import it.unicam.cs.mpgc.rpg130525.port.StudenteDto;

/**
 * Utilità di presentazione che centralizza il formato della barra di stato del
 * giocatore, condiviso dalle view console e JavaFX per rispettare Dont Repeat Yourself.
 */
public final class FormattatoreStato {
    private FormattatoreStato() {
    }

    public static String formatta(StudenteDto studente) {
        return String.format("[%s] HP %d/%d | INTELLIGENZA %d | RESILIENZA %d | CFU %d | Monete %d",
                studente.nomeCompleto(), studente.saluteMentale(), studente.saluteMentaleMax(),
                studente.intelligenzaEffettiva(), studente.resilienzaEffettiva(),
                studente.cfu(), studente.monete());
    }
}
