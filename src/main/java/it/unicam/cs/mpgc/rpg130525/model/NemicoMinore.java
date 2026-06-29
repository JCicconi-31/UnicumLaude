package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Nemico minore del dungeon (Dubbi e Distrazioni, sez. 1.5.1 del documento di
 * analisi): ostacolo psicologico posizionato nei corridoi o nelle aule studio
 * che sottrae Salute Mentale allo studente se non viene affrontato.
 * <p>
 * A differenza dei Boss (i Professori), non possiede un pool di "resistenza" né
 * un combattimento a turni: agisce infliggendo un danno da stress. La classe
 * astratta raccoglie lo stato comune (nome e danno), mentre le sottoclassi
 * concrete rappresentano le singole tipologie. La mitigazione del danno in base
 * alla Resilienza dello studente (sez. 1.3.3) verrà applicata dall'engine
 * (CalcolatoreDanno, sez. 4.1/5), non qui, per non duplicare la logica di gioco.
 * <p>
 * <b>Dichiarazione uso AI:</b> questa classe è stata realizzata con l'assistenza
 * di un'intelligenza artificiale (Claude, Anthropic), come previsto dalle
 * indicazioni del corso sull'utilizzo di strumenti di AI generativa.
 */
public abstract class NemicoMinore {
    private final String nome;
    private final int dannoStress;

    protected NemicoMinore(String nome, int dannoStress) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("nome nemico non valido");
        if (dannoStress <= 0)
            throw new IllegalArgumentException("il danno da stress deve essere positivo");
        this.nome = nome;
        this.dannoStress = dannoStress;
    }

    /**
     * Il nemico attacca lo studente sottraendogli Salute Mentale.
     *
     * @param studente lo studente bersaglio
     * @throws BurnoutException se l'attacco porta la Salute Mentale a zero
     */
    public void attacca(Studente studente) {
        if (studente == null)
            throw new IllegalArgumentException("studente nullo");
        studente.subisciDanno(dannoStress);
    }

    public String getNome() { return nome; }

    public int getDannoStress() { return dannoStress; }
}
