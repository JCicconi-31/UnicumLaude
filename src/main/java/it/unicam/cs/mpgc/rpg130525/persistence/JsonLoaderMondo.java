package it.unicam.cs.mpgc.rpg130525.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import it.unicam.cs.mpgc.rpg130525.model.*;
import it.unicam.cs.mpgc.rpg130525.port.LoaderMondo;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementazione di {@link LoaderMondo} basata su Gson: costruisce la
 * {@code Mappa} di dominio dalla configurazione JSON in due momenti — prima
 * istanzia tutte le stanze, poi collega corridoi e propedeuticità per nome.
 */
public class JsonLoaderMondo implements LoaderMondo {
    private final InputStream input;
    private final Gson gson = new Gson();

    public JsonLoaderMondo(InputStream input) {
        if (input == null) throw new IllegalArgumentException("input nullo");
        this.input = input;
    }

    @Override
    public Mappa caricaMappa() {
        MappaConfigDto config = leggiConfigurazione();
        Map<String, Stanza> perNome = new LinkedHashMap<>();
        Mappa mappa = new Mappa();
        creaStanze(config, mappa, perNome);
        collegaCorridoiEPropedeuticita(config, mappa, perNome);
        return mappa;
    }

    private void creaStanze(MappaConfigDto config, Mappa mappa, Map<String, Stanza> perNome) {
        for (MappaConfigDto.StanzaDto dto : config.stanze()) {
            Stanza stanza = costruisciStanza(dto);
            mappa.addStanza(stanza);
            perNome.put(dto.nome(), stanza);
        }
    }

    private void collegaCorridoiEPropedeuticita(MappaConfigDto config, Mappa mappa, Map<String, Stanza> perNome) {
        for (MappaConfigDto.StanzaDto dto : config.stanze()) {
            Stanza corrente = perNome.get(dto.nome());
            if (dto.adiacenze() != null) for (String nome : dto.adiacenze())
                mappa.addCorridoio(corrente, richiedi(perNome, nome, dto.nome()));
            if (dto.propedeuticita() != null) for (String nome : dto.propedeuticita())
                mappa.addPropedeuticita(richiedi(perNome, nome, dto.nome()), corrente);
        }
    }

    private MappaConfigDto leggiConfigurazione() {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            MappaConfigDto config = gson.fromJson(reader, MappaConfigDto.class);
            if (config == null || config.stanze() == null || config.stanze().isEmpty())
                throw new PersistenceException("configurazione mappa vuota o malformata", null);
            return config;
        } catch (IOException | JsonParseException e) {
            throw new PersistenceException("caricamento mappa fallito", e);
        }
    }

    private Stanza costruisciStanza(MappaConfigDto.StanzaDto dto) {
        try {
            TipoStanza tipo = TipoStanza.valueOf(dto.tipoStanza());
            return new Stanza(dto.nome(), tipo, costruisciEsame(dto.esame()));
        } catch (IllegalArgumentException e) {
            throw new PersistenceException("stanza non valida: " + dto.nome(), e);
        }
    }

    private Esame costruisciEsame(MappaConfigDto.EsameDto dto) {
        if (dto == null) return null;
        MappaConfigDto.ProfessoreDto p = dto.professore();
        if (p == null) throw new PersistenceException("esame '" + dto.nome() + "' senza professore", null);
        Professore professore = new Professore(p.nome(), p.cognome(), p.attacco(), p.hp());
        return new Esame(dto.codiceCorso(), dto.nome(), dto.cfu(), professore);
    }

    private Stanza richiedi(Map<String, Stanza> perNome, String nome, String contesto) {
        Stanza stanza = perNome.get(nome);
        if (stanza == null)
            throw new PersistenceException("la stanza '" + contesto + "' referenzia una stanza inesistente: '" + nome + "'", null);
        return stanza;
    }
}