package it.unicam.cs.mpgc.rpg130525.model.Items;

import it.unicam.cs.mpgc.rpg130525.model.Equipaggiamento;
import it.unicam.cs.mpgc.rpg130525.model.Stat;
import it.unicam.cs.mpgc.rpg130525.model.TipoItem;

import java.util.Map;

public class ChatGPT implements Equipaggiamento {
    private final Map<Stat, Integer> modificatori;

    public ChatGPT() {
        this.modificatori = Map.of(Stat.Intelligenza, 5);
    }

    @Override
    public Map<Stat, Integer> getModificatori() { return modificatori; }

    @Override
    public String getNome() { return "ChatGPT"; }

    @Override
    public TipoItem getTipo() { return TipoItem.ChatGPT; }
}