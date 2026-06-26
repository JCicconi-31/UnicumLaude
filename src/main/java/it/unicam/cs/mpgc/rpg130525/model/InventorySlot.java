package it.unicam.cs.mpgc.rpg130525.model;

public class InventorySlot {
    private final Equipaggiamento item;
    private final int quantita;

    public InventorySlot(Equipaggiamento item, int quantita) {
        this.item = item;
        this.quantita = quantita;
    }

    public int modificatoreSu(Stat stat) {
        return item.getModificatori().getOrDefault(stat, 0) * quantita;
    }

    public Equipaggiamento getItem() { return item; }
    public int getQuantita() { return quantita; }
}
