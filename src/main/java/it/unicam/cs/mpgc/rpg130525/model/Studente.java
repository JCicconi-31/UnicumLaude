package it.unicam.cs.mpgc.rpg130525.model;

import java.util.EnumMap;

public class Studente {
    private final int intelligenzaBase;
    private final int resilienzaBase;
    private int saluteMentale;
    private final int saluteMentaleMax;
    private int cfu;
    private int monete;

    private final EnumMap<TipoItem, InventorySlot> inventario;
    private final CarrerStrategy carriera;

    public Studente(int intelligenzaBase, int resilienzaBase,
                    int saluteMentaleMax, int cfu, int monete,
                    CarrerStrategy carriera) {
        if (intelligenzaBase <= 0)
            throw new IllegalArgumentException("Intelligenza deve essere positiva");
        if (saluteMentaleMax <= 0)
            throw new IllegalArgumentException("HP max deve essere positivo");

        this.carriera = carriera;
        this.intelligenzaBase = intelligenzaBase;
        this.resilienzaBase = resilienzaBase;
        this.saluteMentaleMax = saluteMentaleMax + carriera.modificatoreHpMax();
        this.saluteMentale = this.saluteMentaleMax;
        this.cfu = cfu;
        this.monete = monete + carriera.modificatoreMoneteIniziali();
        this.inventario = new EnumMap<>(TipoItem.class);
    }

    public int getIntelligenzaEffettiva() {
        return intelligenzaBase + inventario.values().stream()
                .mapToInt(slot -> slot.modificatoreSu(Stat.Intelligenza))
                .sum();
    }

    public int getResilienzaEffettiva() {
        return resilienzaBase + inventario.values().stream()
                .mapToInt(slot -> slot.modificatoreSu(Stat.Resilienza))
                .sum();
    }

    public void subisciDanno(int danno) {
        this.saluteMentale -= danno;
        if (this.saluteMentale <= 0) {
            this.saluteMentale = 0;
            throw new BurnoutException("Lo studente ha esaurito le energie");
        }
    }

    public int getSaluteMentale() { return saluteMentale; }
    public int getSaluteMentaleMax() { return saluteMentaleMax; }
    public int getCfu() { return cfu; }
    public int getMonete() { return monete; }
    public EnumMap<TipoItem, InventorySlot> getInventario() { return inventario; }
}
