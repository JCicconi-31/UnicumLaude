package it.unicam.cs.mpgc.rpg130525.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Libretto degli esami superati dallo studente e dei CFU totali accumulati.
 * L'aggiunta di un esame accredita i suoi CFU una sola volta.
 */
public class Libretto {
    private final Set<EsameSuperato> esamiSuperati = new HashSet<>();
    private int cfuOttenuti = 0;

    public boolean addEsameSuperato(EsameSuperato esame) {
        if (esame == null) throw new IllegalArgumentException("esame passato null");
        if (esamiSuperati.add(esame)) {
            aggiungiCFU(esame.esame().cfuAssociati());
            return true;
        }
        return false;
    }

    private void aggiungiCFU(int quantita) {
        this.cfuOttenuti += quantita;
    }

    public int getCfuOttenuti() {
        return cfuOttenuti;
    }

    public Set<Esame> getEsamiSuperati() {
        return esamiSuperati.stream().map(EsameSuperato::esame).collect(Collectors.toUnmodifiableSet());
    }

    public Set<EsameSuperato> getDettaglioEsami() {
        return Collections.unmodifiableSet(esamiSuperati);
    }

}
