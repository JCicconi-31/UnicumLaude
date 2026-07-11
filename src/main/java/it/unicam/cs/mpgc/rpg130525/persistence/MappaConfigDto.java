package it.unicam.cs.mpgc.rpg130525.persistence;

import java.util.List;

/**
 * DTO di trasporto per deserializzare da JSON la configurazione della mappa:
 * stanze con eventuale esame/professore, adiacenze e propedeuticità (per nome).
 * <p>
 * <b>Dichiarazione uso AI:</b> questo record è stato realizzato con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic, tramite Claude Code), come
 * previsto dalle indicazioni del corso.
 */
public record MappaConfigDto(List<StanzaDto> stanze) {
    public record StanzaDto(String nome, String tipoStanza, EsameDto esame, List<String> adiacenze,
                            List<String> propedeuticita) {
    }

    public record EsameDto(int codiceCorso, String nome, int cfu, ProfessoreDto professore) {
    }

    public record ProfessoreDto(String nome, String cognome, int hp, int attacco) {
    }
}