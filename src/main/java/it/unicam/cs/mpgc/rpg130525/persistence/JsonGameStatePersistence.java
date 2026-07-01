package it.unicam.cs.mpgc.rpg130525.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class JsonGameStatePersistence implements PersistenceManager {
    private final Path file;
    private final Mappa mappa;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonGameStatePersistence(Path file, Mappa mappa) {
        if (file == null || mappa == null)
            throw new IllegalArgumentException("file o mappa nulli");
        this.file = file;
        this.mappa = mappa;
    }

    @Override
    public void salva(StatoGioco stato) {
        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(versoDto(stato), w);
        } catch (IOException e) {
            throw new PersistenceException("salvataggio fallito", e);
        }
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
        var esami = s.getLibretto().getDettaglioEsami().stream()
                .map(e -> new GameStateDto.EsameSuperatoDto(e.getEsame().getCodiceCorso(), e.getVoto()))
                .toList();
        var inventario = s.getInventario().entrySet().stream()
                .collect(Collectors.toMap(en -> en.getKey().name(), en -> en.getValue().getQuantita()));
        return new GameStateDto(
                s.getNome(), s.getCognome(),
                s.getIntelligenzaBase(), s.getResilienzaBase(),
                s.getSaluteMentaleMax() - c.modificatoreHpMax(),
                s.getMonete() - c.modificatoreMoneteIniziali(),
                c.getClass().getSimpleName(),
                s.getSaluteMentale(),
                stato.getPosizioneCorrente().getNome(),
                esami, inventario);
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
            if (st.getEsame() != null)
                esamiPerCodice.put(st.getEsame().getCodiceCorso(), st.getEsame());
        }
        for (var e : dto.esamiSuperati()) {
            Esame esame = esamiPerCodice.get(e.codiceCorso());
            if (esame != null)
                s.getLibretto().addEsameSuperato(new EsameSuperato(esame, e.voto()));
        }
        return new StatoGioco(s, stanzePerNome.get(dto.nomeStanzaCorrente()));
    }

    private CareerStrategy carrieraDa(String tipo) {
        return switch (tipo) {
            case "StudenteFullTime"   -> new StudenteFullTime();
            case "StudenteLavoratore" -> new StudenteLavoratore();
            case "StudenteFuoriCorso" -> new StudenteFuoriCorso();
            default -> throw new PersistenceException("carriera sconosciuta: " + tipo, null);
        };
    }
}