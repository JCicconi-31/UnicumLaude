package it.unicam.cs.mpgc.rpg130525.model.Items;

import it.unicam.cs.mpgc.rpg130525.model.Equipaggiamento;
import it.unicam.cs.mpgc.rpg130525.model.Stat;
import it.unicam.cs.mpgc.rpg130525.model.TipoItem;

import java.util.Map;

/**
 * Equipaggiamento che fornisce un bonus all'intelligenza.
 */
public class Libro implements Equipaggiamento {
    @Override
    public Map<Stat, Integer> getModificatori() {
        return Map.of(Stat.INTELLIGENZA, 3);
    }

    @Override
    public String getNome() {
        return "LIBRO di Testo";
    }

    @Override
    public TipoItem getTipo() {
        return TipoItem.LIBRO;
    }
}
