package it.unicam.cs.mpgc.rpg130525.model.Items;

import it.unicam.cs.mpgc.rpg130525.model.Equipaggiamento;
import it.unicam.cs.mpgc.rpg130525.model.Stat;
import it.unicam.cs.mpgc.rpg130525.model.TipoItem;
import java.util.Map;

public class Libro implements Equipaggiamento {
    @Override public Map<Stat, Integer> getModificatori() {
        return Map.of(Stat.Intelligenza, 3);
    }

    @Override public String getNome() {
        return "Libro di Testo";
    }

    @Override public TipoItem getTipo() {
        return TipoItem.Libro;
    }
}
