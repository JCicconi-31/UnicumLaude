package it.unicam.cs.mpgc.rpg130525.port;

import it.unicam.cs.mpgc.rpg130525.model.Professore;

/**
 * DTO di sola lettura con lo stato del professore da mostrare nella view
 * (nome e HP correnti/massimi durante la prova orale).
 */
public record ProfessoreDto(String nome, int hp, int hpMax) {
    public static ProfessoreDto da(Professore p) {
        return new ProfessoreDto(p.getNomeCompleto(), p.getHpProfessore(), p.getHpMax());
    }
}