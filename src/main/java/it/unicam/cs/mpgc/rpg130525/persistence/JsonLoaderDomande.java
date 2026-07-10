package it.unicam.cs.mpgc.rpg130525.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import it.unicam.cs.mpgc.rpg130525.model.Domanda;
import it.unicam.cs.mpgc.rpg130525.port.LoaderDomande;
import it.unicam.cs.mpgc.rpg130525.port.PersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonLoaderDomande implements LoaderDomande {
    private final InputStream input;
    private final Gson gson = new Gson();

    public JsonLoaderDomande(InputStream input) {
        if (input == null) throw new IllegalArgumentException("input nullo");
        this.input = input;
    }

    @Override
    public Map<String, List<Domanda>> caricaDomande() {
        DomandeConfigDto config = leggiConfigurazione();
        Map<String, List<Domanda>> perCorso = new LinkedHashMap<>();
        for (DomandeConfigDto.CorsoDto corso : config.corsi())
            perCorso.put(corso.nomeCorso(), costruisciDomande(corso));
        return perCorso;
    }

    private DomandeConfigDto leggiConfigurazione() {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            DomandeConfigDto config = gson.fromJson(reader, DomandeConfigDto.class);
            if (config == null || config.corsi() == null || config.corsi().isEmpty())
                throw new PersistenceException("configurazione domande vuota o malformata", null);
            return config;
        } catch (IOException | JsonParseException e) {
            throw new PersistenceException("caricamento domande fallito", e);
        }
    }

    private List<Domanda> costruisciDomande(DomandeConfigDto.CorsoDto corso) {
        if (corso.domande() == null || corso.domande().isEmpty())
            throw new PersistenceException("nessuna domanda per il corso: " + corso.nomeCorso(), null);
        List<Domanda> domande = new ArrayList<>();
        try {
            for (DomandeConfigDto.DomandaDto dto : corso.domande())
                domande.add(new Domanda(dto.testo(), dto.opzioni(), dto.indiceCorretta()));
        } catch (IllegalArgumentException e) {
            throw new PersistenceException("domanda non valida nel corso: " + corso.nomeCorso(), e);
        }
        return domande;
    }
}