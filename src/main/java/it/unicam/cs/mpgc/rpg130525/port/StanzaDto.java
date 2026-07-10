package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.TipoStanza;

import java.util.List;

public record StanzaDto(String nome, TipoStanza tipo, Stato stato, List<String> adiacenti) {
    public enum Stato {BLOCCATA, DISPONIBILE, SUPERATA}
}