package it.unicam.cs.mpgc.rpg130525.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementazione di {@link PersistenceManager} basata su Gson: salva e carica
 * lo stato di gioco su un file JSON, riagganciando in fase di caricamento stanze
 * ed esami alle istanze reali della mappa.
 */
public class JsonGameStatePersistence implements PersistenceManager {
    private final Path file;
    private final Mappa mappa;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonGameStatePersistence(Path file, Mappa mappa) {
        if (file == null || mappa == null) throw new IllegalArgumentException("file o mappa nulli");
        this.file = file;
        this.mappa = mappa;
    }

    @Override
    public void salva(StatoGioco stato) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) {
                gson.toJson(versoDto(stato), w);
            }
        } catch (IOException e) {
            throw new PersistenceException("salvataggio fallito", e);
        }
    }

    @Override
    public boolean esisteSalvataggio() {
        return Files.exists(file);
    }

    @Override
    public StatoGioco carica() {
        try (Reader r = Files.newBufferedReader(file)) {
            return daDto(gson.fromJson(r, GameStateDto.class));
        } catch (IOException e) {
            throw new PersistenceException("caricamento fallito", e);
        }
    }

    private GameStateDto versoDto(StatoGioco stato) {
        Studente s = stato.getStudente();
        CareerStrategy c = s.getCarriera();
        return new GameStateDto(s.getNome(), s.getCognome(), s.getIntelligenzaBase(), s.getResilienzaBase(),
                s.getSaluteMentaleMax() - c.modificatoreHpMax(), s.getMonete() - c.modificatoreMoneteIniziali(),
                c.getClass().getSimpleName(), s.getSaluteMentale(), stato.getPosizioneCorrente().getNome(),
                esamiVersoDto(s), inventarioVersoDto(s));
    }

    private List<GameStateDto.EsameSuperatoDto> esamiVersoDto(Studente studente) {
        return studente.getLibretto().getDettaglioEsami().stream()
                .map(e -> new GameStateDto.EsameSuperatoDto(e.esame().codiceCorso(), e.voto()))
                .toList();
    }

    private Map<String, Integer> inventarioVersoDto(Studente studente) {
        Map<String, Integer> inventario = new HashMap<>();
        studente.getInventario().forEach((tipo, slot) -> inventario.put(tipo.name(), slot.getQuantita()));
        for (Consumabile cons : studente.getZaino())
            inventario.merge(cons.getTipo().name(), 1, Integer::sum);
        return inventario;
    }

    private StatoGioco daDto(GameStateDto dto) {
        Studente studente = ricreaStudente(dto);
        ripristinaEsamiSuperati(studente, dto.esamiSuperati());
        ripristinaInventario(studente, dto.inventario());
        Stanza posizione = mappa.getStanza(dto.nomeStanzaCorrente())
                .orElseThrow(() -> new PersistenceException(
                        "la stanza del salvataggio non esiste nella mappa: " + dto.nomeStanzaCorrente(), null));
        return new StatoGioco(studente, posizione);
    }

    private Studente ricreaStudente(GameStateDto dto) {
        Studente studente = new Studente(dto.nome(), dto.cognome(), dto.intelligenzaBase(), dto.resilienzaBase(),
                dto.saluteMentaleMaxBase(), dto.moneteBase(), carrieraDa(dto.tipoCarriera()));
        int delta = studente.getSaluteMentaleMax() - dto.saluteMentaleAttuale();
        if (delta > 0) studente.subisciDanno(delta);
        return studente;
    }

    private void ripristinaEsamiSuperati(Studente studente, List<GameStateDto.EsameSuperatoDto> esamiSuperati) {
        Map<Integer, Esame> esamiPerCodice = new HashMap<>();
        for (Stanza st : mappa.getStanze())
            if (st.getEsame() != null) esamiPerCodice.put(st.getEsame().codiceCorso(), st.getEsame());
        for (var e : esamiSuperati) {
            Esame esame = esamiPerCodice.get(e.codiceCorso());
            if (esame != null) studente.getLibretto().addEsameSuperato(new EsameSuperato(esame, e.voto()));
        }
    }

    private void ripristinaInventario(Studente studente, Map<String, Integer> inventario) {
        for (var voce : inventario.entrySet()) {
            TipoItem tipo = TipoItem.valueOf(voce.getKey());
            for (int i = 0; i < voce.getValue(); i++)
                CatalogoItem.crea(tipo).aggiungiA(studente);
        }
    }

    private CareerStrategy carrieraDa(String tipo) {
        return switch (tipo) {
            case "StudenteFullTime" -> new StudenteFullTime();
            case "StudenteLavoratore" -> new StudenteLavoratore();
            case "StudenteFuoriCorso" -> new StudenteFuoriCorso();
            default -> throw new PersistenceException("carriera sconosciuta: " + tipo, null);
        };
    }
}