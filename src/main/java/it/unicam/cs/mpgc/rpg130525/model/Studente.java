package it.unicam.cs.mpgc.rpg130525.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Studente extends Persona{
    private final int intelligenzaBase;
    private final int resilienzaBase;
    private int saluteMentale;
    private final int saluteMentaleMax;
    private int cfu;
    private int monete;

    private final EnumMap<TipoItem, InventorySlot> inventario;
    private final List<Consumabile> zaino;
    private final CareerStrategy carriera;

    public Studente(String nome, String cognome, int intelligenzaBase, int resilienzaBase,
                    int saluteMentaleMax, int cfu, int monete,
                    CareerStrategy carriera) {
        super(nome,cognome);
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
        this.zaino = new ArrayList<>();
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

    public void recuperaHP(int quantita) {
        this.saluteMentale = Math.min(saluteMentale + quantita, saluteMentaleMax);
    }

    public void aggiungiCFU(int quantita) {
        this.cfu += quantita;
    }

    public void equipaggiaItem(Equipaggiamento item) {
        inventario.put(item.getTipo(), new InventorySlot(item, 1));
    }

    public void aggiungiConsumabile(Consumabile consumabile) {
        zaino.add(consumabile);
    }

    public void usaConsumabile(Consumabile consumabile) {
        if (zaino.remove(consumabile))
            consumabile.applica(this);
    }

    public int getSaluteMentale() { return saluteMentale; }
    public int getSaluteMentaleMax() { return saluteMentaleMax; }
    public int getCFU() { return cfu; }
    public int getMonete() { return monete; }
    public EnumMap<TipoItem, InventorySlot> getInventario() { return inventario; }
    public List<Consumabile> getZaino() { return zaino; }
}
