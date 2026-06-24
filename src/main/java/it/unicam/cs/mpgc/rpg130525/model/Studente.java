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
    private final CareerStrategy carriera;

    public Studente(int intelligenzaBase, int resilienzaBase,
                    int saluteMentaleMax, int cfu, int monete,
                    CareerStrategy carriera) {
        if (intelligenzaBase <= 0)
            throw new IllegalArgumentException("Intelligenza deve essere positiva");
        if (saluteMentaleMax <= 0)
            throw new IllegalArgumentException("HP max deve essere positivo");

        this.intelligenzaBase = intelligenzaBase;
        this.resilienzaBase = resilienzaBase;
        this.saluteMentaleMax = saluteMentaleMax;
        this.saluteMentale = saluteMentaleMax;
        this.cfu = cfu;
        this.monete = monete;
        this.inventario = new EnumMap<>(TipoItem.class);
        this.carriera = carriera;
        carriera.aggiungiStudente(this);
    }


}
