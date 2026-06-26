package it.unicam.cs.mpgc.rpg130525.model;

import java.util.Objects;

public class Esame {
    private int codiceCorso;
    private String nomeCorso;
    private int cfuAssociati;
    private Professore professore;

    public Esame(int codiceCorso, int cfuAssociati, String nomeCorso, Professore professore) {
        setCodiceCorso(codiceCorso);
        setCfuAssociati(cfuAssociati);
        setNomeCorso(nomeCorso);
        setProfessore(professore);
    }


    public int getCodiceCorso() {
        return codiceCorso;
    }
    public void setCodiceCorso(int codiceCorso) {
        if (codiceCorso < 0)
            throw new IllegalArgumentException("Codice corso non valido");
        this.codiceCorso = codiceCorso;
    }
    public Professore getProfessore() {
        return professore;
    }
    public void setProfessore(Professore professore) {
        if (professore == null)
            throw new IllegalArgumentException("Professore non valido");
        this.professore = professore;
    }
    public int getCfuAssociati() {
        return cfuAssociati;
    }
    public void setCfuAssociati(int cfuAssociati) {
        if (cfuAssociati <= 0 || cfuAssociati > 18)
            throw new IllegalArgumentException("Numero CFU non valido");
        this.cfuAssociati = cfuAssociati;
    }
    public String getNomeCorso() {
        return nomeCorso;
    }
    public void setNomeCorso(String nomeCorso) {
        if (nomeCorso == null || nomeCorso.isBlank())
            throw new IllegalArgumentException("Nome esame non valido");
        this.nomeCorso = nomeCorso;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Esame esame = (Esame) o;
        return codiceCorso == esame.codiceCorso && cfuAssociati == esame.cfuAssociati && Objects.equals(nomeCorso, esame.nomeCorso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codiceCorso, nomeCorso, cfuAssociati);
    }
}
