package it.unicam.cs.mpgc.rpg130525.model;

import java.util.*;

public class Studente extends Persona {
    private final int intelligenzaBase;
    private final int resilienzaBase;
    private final int saluteMentaleMax;
    private final Libretto libretto;
    private final EnumMap<TipoItem, InventorySlot> inventario;
    private final List<Consumabile> zaino;
    private final CareerStrategy carriera;
    private int saluteMentale;
    private int monete;

    public Studente(String nome, String cognome, int intelligenzaBase, int resilienzaBase, int saluteMentaleMax, int monete, CareerStrategy carriera) {
        super(nome, cognome);
        if (intelligenzaBase <= 0) throw new IllegalArgumentException("INTELLIGENZA deve essere positiva");
        if (saluteMentaleMax <= 0) throw new IllegalArgumentException("HP max deve essere positivo");
        if (carriera == null) throw new IllegalArgumentException("carriera nulla");
        if (resilienzaBase < 0) throw new IllegalArgumentException("resilienza non può essere negativa");
        if (monete < 0) throw new IllegalArgumentException("le monete non possono essere negative");

        this.carriera = carriera;
        this.intelligenzaBase = intelligenzaBase;
        this.resilienzaBase = resilienzaBase;
        this.saluteMentaleMax = saluteMentaleMax + carriera.modificatoreHpMax();
        this.saluteMentale = this.saluteMentaleMax;
        this.monete = monete + carriera.modificatoreMoneteIniziali();
        this.inventario = new EnumMap<>(TipoItem.class);
        this.zaino = new ArrayList<>();
        this.libretto = new Libretto();
    }

    public int getIntelligenzaEffettiva() {
        return intelligenzaBase + inventario.values().stream().mapToInt(slot -> slot.modificatoreSu(Stat.INTELLIGENZA)).sum();
    }

    public int getResilienzaEffettiva() {
        return resilienzaBase + inventario.values().stream().mapToInt(slot -> slot.modificatoreSu(Stat.RESILIENZA)).sum();
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

    public void equipaggiaItem(Equipaggiamento item) {
        inventario.put(item.getTipo(), new InventorySlot(item, 1));
    }

    public void aggiungiConsumabile(Consumabile consumabile) {
        zaino.add(consumabile);
    }

    public void usaConsumabile(Consumabile consumabile) {
        if (zaino.remove(consumabile)) consumabile.applica(this);
    }

    public int getSaluteMentale() {
        return saluteMentale;
    }

    public int getSaluteMentaleMax() {
        return saluteMentaleMax;
    }

    public int getMonete() {
        return monete;
    }

    public Map<TipoItem, InventorySlot> getInventario() {
        return Collections.unmodifiableMap(inventario);
    }

    public List<Consumabile> getZaino() {
        return Collections.unmodifiableList(zaino);
    }

    public Libretto getLibretto() {
        return libretto;
    }

    public int getIntelligenzaBase() {
        return intelligenzaBase;
    }

    public int getResilienzaBase() {
        return resilienzaBase;
    }

    public CareerStrategy getCarriera() {
        return carriera;
    }

    public void spendiMonete(int costo) {
        if (costo > getMonete()) throw new IllegalArgumentException("costo maggiore monete possedute");
        this.monete = monete - costo;
    }
}
