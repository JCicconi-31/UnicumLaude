package it.unicam.cs.mpgc.rpg130525.model;

import java.util.*;

public class Mappa {
    private final Map<Stanza, Set<Stanza>> collegamenti = new HashMap<>();
    private final Map<Stanza, Set<Stanza>> propedeuticita = new HashMap<>();

    public void addStanza(Stanza stanza) {
        if (!collegamenti.containsKey(stanza))
            collegamenti.put(stanza, new HashSet<>());
        if (!propedeuticita.containsKey(stanza))
            propedeuticita.put(stanza, new HashSet<>());
    }

    public void addCorridoio(Stanza a, Stanza b) {
        collegamenti.get(a).add(b);
        collegamenti.get(b).add(a);
    }

    public void addPropedeuticita(Stanza prerequisito, Stanza successiva) {
        propedeuticita.get(successiva).add(prerequisito);
    }

    public Set<Stanza> getAdiacenti(Stanza stanza) {
        return Collections.unmodifiableSet(collegamenti.getOrDefault(stanza, Set.of()));
    }

    //verifica se una stanza è accessibile secondo gli esami superati dal giocatore.
    public boolean isDisponibile(Stanza stanza, Set<Esame> esamiSuperati) {
        return propedeuticita.getOrDefault(stanza, Set.of())
                .stream()
                .allMatch(prerequisito -> esamiSuperati.contains(prerequisito.getEsame()));
    }

    public Set<Stanza> getStanze() {
        return Collections.unmodifiableSet(collegamenti.keySet());
    }

    public Optional<Stanza> getStanza(String nome) {
        return collegamenti.keySet().stream()
                .filter(s -> s.getNome().equals(nome))
                .findFirst();
    }

    public int cfuTotali() {
        return collegamenti.keySet().stream()
                .map(Stanza::getEsame)
                .filter(Objects::nonNull)
                .mapToInt(Esame::getCfuAssociati)
                .sum();
    }
}
