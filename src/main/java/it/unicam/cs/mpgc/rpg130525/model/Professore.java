package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Avversario di un'aula d'esame: ha una difficoltà (danno inflitto) e degli HP
 * che calano durante la prova orale finché non va KO.
 */
public class Professore extends Persona {
    private final int difficolta;
    private final int hpMax;
    private int hpProfessore;

    public Professore(String nome, String cognome, int difficolta, int hpMax) {
        super(nome, cognome);
        if (difficolta <= 0) throw new IllegalArgumentException("difficoltà negativa");
        if (hpMax <= 0) throw new IllegalArgumentException("resistenza negativa");
        this.difficolta = difficolta;
        this.hpMax = hpMax;
        this.hpProfessore = hpMax;
    }

    public void subisciDanno(int danno) {
        this.hpProfessore = hpProfessore - danno;
        if (isKO()) hpProfessore = 0;
    }

    public boolean isKO() {
        return hpProfessore <= 0;
    }

    public int getDifficolta() {
        return difficolta;
    }

    public int getHpProfessore() {
        return hpProfessore;
    }

    public int getHpMax() {
        return hpMax;
    }
}
