package it.unicam.cs.mpgc.rpg130525.persistence;

import java.util.List;
import java.util.Map;

public record GameStateDto(
        // attributi statici
        String nome, String cognome, int intelligenzaBase, int resilienzaBase, int saluteMentaleMaxBase, int moneteBase,
        String tipoCarriera,
        // attributi dinamici
        int saluteMentaleAttuale, String nomeStanzaCorrente, List<EsameSuperatoDto> esamiSuperati,
        Map<String, Integer> inventario) {
    public record EsameSuperatoDto(int codiceCorso, int voto) {
    }
}
