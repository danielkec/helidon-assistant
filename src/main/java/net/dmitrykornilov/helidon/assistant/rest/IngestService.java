package net.dmitrykornilov.helidon.assistant.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;

import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import net.dmitrykornilov.helidon.assistant.rag.DocsIngestor;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service.Singleton
public class IngestService implements HttpService {

    private final DocsIngestor docsIngestor;

    @Service.Inject
    public IngestService(DocsIngestor docsIngestor) {
        this.docsIngestor = docsIngestor;
    }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::ingest);
    }

    private void ingest(ServerRequest req, ServerResponse res) {
        docsIngestor.clear();

        var osw = new OutputStreamWriter(res.outputStream(), UTF_8);
        docsIngestor.ingest(progress -> {
            try {
                osw.append(String.valueOf(progress));
                osw.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
