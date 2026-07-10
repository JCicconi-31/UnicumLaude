package it.unicam.cs.mpgc.rpg130525.model;

import java.util.Objects;

public record EsameSuperato(Esame esame, int voto) {
    public EsameSuperato {
        if (esame == null) throw new IllegalArgumentException("Esame non valido");
        if (voto < 18 || voto > 31) throw new IllegalArgumentException("Voto non valido");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(esame, ((EsameSuperato) o).esame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(esame);
    }
}
