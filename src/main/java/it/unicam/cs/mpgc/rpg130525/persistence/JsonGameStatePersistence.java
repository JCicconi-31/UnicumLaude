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
import java.util.Map;

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
        var esami = s.getLibretto().getDettaglioEsami().stream().map(e -> new GameStateDto.EsameSuperatoDto(e.esame().codiceCorso(), e.voto())).toList();
        Map<String, Integer> inventario = new HashMap<>();
        s.getInventario().forEach((tipo, slot) -> inventario.put(tipo.name(), slot.getQuantita()));
        for (Consumabile cons : s.getZaino())
            inventario.merge(cons.getTipo().name(), 1, Integer::sum);
        return new GameStateDto(s.getNome(), s.getCognome(), s.getIntelligenzaBase(), s.getResilienzaBase(), s.getSaluteMentaleMax() - c.modificatoreHpMax(), s.getMonete() - c.modificatoreMoneteIniziali(), c.getClass().getSimpleName(), s.getSaluteMentale(), stato.getPosizioneCorrente().getNome(), esami, inventario);
    }

    private StatoGioco daDto(GameStateDto dto) {
        CareerStrategy carriera = carrieraDa(dto.tipoCarriera());
        Studente s = new Studente(dto.nome(), dto.cognome(), dto.intelligenzaBase(), dto.resilienzaBase(), dto.saluteMentaleMaxBase(), dto.moneteBase(), carriera);
        int delta = s.getSaluteMentaleMax() - dto.saluteMentaleAttuale();
        if (delta > 0) s.subisciDanno(delta);
        Map<Integer, Esame> esamiPerCodice = new HashMap<>();
        Map<String, Stanza> stanzePerNome = new HashMap<>();
        for (Stanza st : mappa.getStanze()) {
            stanzePerNome.put(st.getNome(), st);
            if (st.getEsame() != null) esamiPerCodice.put(st.getEsame().codiceCorso(), st.getEsame());
        }
        for (var e : dto.esamiSuperati()) {
            Esame esame = esamiPerCodice.get(e.codiceCorso());
            if (esame != null) s.getLibretto().addEsameSuperato(new EsameSuperato(esame, e.voto()));
        }
        for (var voce : dto.inventario().entrySet()) {
            TipoItem tipo = TipoItem.valueOf(voce.getKey());
            for (int i = 0; i < voce.getValue(); i++) {
                Item item = CatalogoItem.crea(tipo);
                item.aggiungiA(s);
            }
        }
        return new StatoGioco(s, stanzePerNome.get(dto.nomeStanzaCorrente()));
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