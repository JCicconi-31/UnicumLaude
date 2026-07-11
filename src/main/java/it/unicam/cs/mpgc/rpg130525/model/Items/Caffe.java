package it.unicam.cs.mpgc.rpg130525.model.Items;

import it.unicam.cs.mpgc.rpg130525.model.Consumabile;
import it.unicam.cs.mpgc.rpg130525.model.Studente;
import it.unicam.cs.mpgc.rpg130525.model.TipoItem;

/**
 * Consumabile che recupera salute mentale allo studente.
 */
public class Caffe implements Consumabile {
    private final int recuperoHp;

    public Caffe(int recuperoHp) {
        if (recuperoHp <= 0) throw new IllegalArgumentException("recupero HP non valido");
        this.recuperoHp = recuperoHp;
    }

    @Override
    public void applica(Studente studente) {
        studente.recuperaHP(recuperoHp);
    }

    @Override
    public String getNome() {
        return "CAFFE";
    }

    @Override
    public TipoItem getTipo() {
        return TipoItem.CAFFE;
    }
}