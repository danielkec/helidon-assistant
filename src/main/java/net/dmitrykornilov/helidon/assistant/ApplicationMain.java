package net.dmitrykornilov.helidon.assistant;

import java.time.Duration;
import java.time.Instant;

import io.helidon.config.Config;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

import net.dmitrykornilov.helidon.assistant.rag.DocsIngestor;
import net.dmitrykornilov.helidon.assistant.rest.ChatBotService;
import net.dmitrykornilov.helidon.assistant.rest.IngestService;

public class ApplicationMain {

    private static final Header UI_REDIRECT = HeaderValues.createCached(HeaderNames.LOCATION, "/ui");

    public static void main(String[] args) {
        // Make sure logging is enabled as the first thing
        LogConfig.configureRuntime();

        var config = Services.get(Config.class);

        var startTime = Instant.now();

        var ingestor = Services.get(DocsIngestor.class);
        ingestor.clear();
        ingestor.ingest((done, total) -> {
            System.out.printf("\r Ingesting RAG [%s%s] %d/%d", "=".repeat(done), " ".repeat(total - done), done, total);
        });
        System.out.println();
        System.out.println("Ingestion took " + Duration.between(startTime, Instant.now()));

        WebServer.builder()
                .config(config.get("server"))
                .routing(ApplicationMain::routing)
                .build()
                .start();
    }

    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing.any("/", (req, res) -> {
                    // showing the capability to run on any path, and redirecting from root
                    res.status(Status.MOVED_PERMANENTLY_301);
                    res.headers().set(UI_REDIRECT);
                    res.send();
                })
                .register("/ingest", Services.get(IngestService.class))
                .register("/chat", Services.get(ChatBotService.class));
    }
}
