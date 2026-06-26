package it.unicam.cs.mpgc.rpg130525.model;

public class Esame {
    private final int codiceCorso;
    private final String nomeCorso;
    private final int cfuAssociati;
    private final Professore professore;

    public Esame(int codiceCorso, String nomeCorso, int cfuAssociati, Professore professore) {
        if (codiceCorso < 0)
            throw new IllegalArgumentException("Codice corso non valido");
        if (nomeCorso == null || nomeCorso.isBlank())
            throw new IllegalArgumentException("Nome esame non valido");
        if (cfuAssociati <= 0 || cfuAssociati > 18)
            throw new IllegalArgumentException("Numero CFU non valido");
        if (professore == null)
            throw new IllegalArgumentException("Professore non valido");
        this.codiceCorso = codiceCorso;
        this.nomeCorso = nomeCorso;
        this.cfuAssociati = cfuAssociati;
        this.professore = professore;
    }

    public int getCodiceCorso() { return codiceCorso; }
    public String getNomeCorso() { return nomeCorso; }
    public int getCfuAssociati() { return cfuAssociati; }
    public Professore getProfessore() { return professore; }

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
