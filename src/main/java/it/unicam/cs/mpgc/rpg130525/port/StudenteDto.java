package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Studente;

/**
 * DTO di sola lettura con lo snapshot dello studente da mostrare nella view
 * (nome, salute mentale, statistiche effettive, CFU e monete).
 */
public record StudenteDto(String nomeCompleto, int saluteMentale, int saluteMentaleMax, int intelligenzaEffettiva,
                          int resilienzaEffettiva, int cfu, int monete) {
    public static StudenteDto da(Studente s) {
        return new StudenteDto(s.getNomeCompleto(), s.getSaluteMentale(), s.getSaluteMentaleMax(), s.getIntelligenzaEffettiva(), s.getResilienzaEffettiva(), s.getLibretto().getCfuOttenuti(), s.getMonete());
    }
}