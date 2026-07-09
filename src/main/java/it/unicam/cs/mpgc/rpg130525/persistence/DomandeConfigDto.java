package it.unicam.cs.mpgc.rpg130525.persistence;

import java.util.List;

public record DomandeConfigDto(List<CorsoDto> corsi) {
    public record CorsoDto(String nomeCorso, List<DomandaDto> domande) { }
    public record DomandaDto(String testo, List<String> opzioni, int indiceCorretta) { }
}