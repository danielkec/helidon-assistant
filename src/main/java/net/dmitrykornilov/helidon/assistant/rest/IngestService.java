package net.dmitrykornilov.helidon.assistant.rest;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import net.dmitrykornilov.helidon.assistant.rag.DocsIngestor;

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
        var writer = new PrintStream(res.outputStream(), true, StandardCharsets.UTF_8);
        docsIngestor.ingest((done, total) -> writer.println(done + "/" + total));
    }
}
