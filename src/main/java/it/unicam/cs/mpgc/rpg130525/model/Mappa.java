package it.unicam.cs.mpgc.rpg130525.model;

import java.util.*;

/**
 * Grafo delle stanze del gioco: gestisce i corridoi (archi non orientati), le
 * propedeuticità (vincoli d'accesso) e le interrogazioni sul mondo (ricerca per
 * nome, CFU totali, disponibilità di una stanza).
 */
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

    /**
     * Verifica se una stanza è accessibile dati gli esami superati dal giocatore.
     *
     * @return true se tutte le propedeuticità della stanza sono soddisfatte
     * (una stanza senza propedeuticità è sempre disponibile)
     */
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
                .mapToInt(Esame::cfuAssociati)
                .sum();
    }
}
