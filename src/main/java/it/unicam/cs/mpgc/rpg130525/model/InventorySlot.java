package it.unicam.cs.mpgc.rpg130525.model;

/**
 * Slot d'inventario che associa un equipaggiamento a una quantità e ne calcola
 * il contributo complessivo a una statistica.
 */
public class InventorySlot {
    private final Equipaggiamento item;
    private final int quantita;

    public InventorySlot(Equipaggiamento item, int quantita) {
        if (item == null)
            throw new IllegalArgumentException("equipaggiamento nullo");
        if (quantita <= 0)
            throw new IllegalArgumentException("quantità negativo");
        this.item = item;
        this.quantita = quantita;
    }

    public int modificatoreSu(Stat stat) {
        return item.getModificatori().getOrDefault(stat, 0) * quantita;
    }

    public int getQuantita() {
        return quantita;
    }
}
