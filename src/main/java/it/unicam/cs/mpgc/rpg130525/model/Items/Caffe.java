package it.unicam.cs.mpgc.rpg130525.model.Items;
import it.unicam.cs.mpgc.rpg130525.model.*;

public class Caffe implements Consumabile {
    private final int recuperoHp;

    public Caffe(int recuperoHp) {
        if (recuperoHp <= 0)
            throw new IllegalArgumentException("recupero HP non valido");
        this.recuperoHp = recuperoHp;
    }

    @Override
    public void applica(Studente studente) {
        studente.recuperaHP(recuperoHp);
    }

    @Override
    public String getNome() { return "Caffè"; }

    @Override
    public TipoItem getTipo() { return TipoItem.Caffè; }
}