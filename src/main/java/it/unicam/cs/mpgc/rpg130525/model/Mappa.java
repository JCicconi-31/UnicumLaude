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

    public void addPropedeuticita(Stanza prerequisito, Stanza successiva) {
        propedeuticita.get(successiva).add(prerequisito);
    }

    public Set<Stanza> getAdiacenti(Stanza stanza) {
        return Collections.unmodifiableSet(collegamenti.getOrDefault(stanza, Set.of()));
    }

    //TODO
    public void isDisponible(Stanza stanza, Set<Esame> esamiSuperati){}
}
