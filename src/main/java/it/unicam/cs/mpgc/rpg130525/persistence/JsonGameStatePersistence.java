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
        //TODO
        return null;
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