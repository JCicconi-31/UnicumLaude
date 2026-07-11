package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Esame universitario: codice del corso, nome, CFU assegnati e professore che lo
 * esamina.
 */
public record Esame(int codiceCorso, String nomeCorso, int cfuAssociati, Professore professore) {
    public Esame {
        if (codiceCorso < 0) throw new IllegalArgumentException("codice corso non valido");
        if (nomeCorso == null || nomeCorso.isBlank()) throw new IllegalArgumentException("nome esame non valido");
        if (cfuAssociati <= 0 || cfuAssociati > 18) throw new IllegalArgumentException("numero CFU non valido");
        if (professore == null) throw new IllegalArgumentException("professore non valido");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return codiceCorso == ((Esame) o).codiceCorso;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codiceCorso);
    }
}
