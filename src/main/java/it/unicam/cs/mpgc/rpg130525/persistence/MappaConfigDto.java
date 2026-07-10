package it.unicam.cs.mpgc.rpg130525.persistence;

import java.util.List;

public record MappaConfigDto(List<StanzaDto> stanze) {
    public record StanzaDto(String nome, String tipoStanza, EsameDto esame, List<String> adiacenze,
                            List<String> propedeuticita) {
    }

    public record EsameDto(int codiceCorso, String nome, int cfu, ProfessoreDto professore) {
    }

    public record ProfessoreDto(String nome, String cognome, int hp, int attacco) {
    }
}