package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.TipoStanza;

import java.util.List;

/**
 * DTO di sola lettura con lo stato di una stanza per il rendering della mappa:
 * nome, tipo, stato di accessibilità e nomi delle stanze adiacenti.
 */
public record StanzaDto(String nome, TipoStanza tipo, Stato stato, List<String> adiacenti) {
    public enum Stato {BLOCCATA, DISPONIBILE, SUPERATA}
}