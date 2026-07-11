package it.unicam.cs.mpgc.rpg130525.persistence;

import java.util.List;

/**
 * DTO di trasporto per deserializzare da JSON la configurazione delle domande,
 * raggruppate per corso.
 */
public record DomandeConfigDto(List<CorsoDto> corsi) {
    public record CorsoDto(String nomeCorso, List<DomandaDto> domande) {
    }

    public record DomandaDto(String testo, List<String> opzioni, int indiceCorretta) {
    }
}