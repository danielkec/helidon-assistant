package net.dmitrykornilov.helidon.assistant.rag;

import java.io.File;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.config.Config;
import io.helidon.config.ConfigException;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

@Service.Singleton
public class DocsIngestor {
    private static final Logger LOGGER = System.getLogger(DocsIngestor.class.getName());

    private final Config config;
    private final AsciiDocPreprocessor preprocessor = new AsciiDocPreprocessor();
    private final EmbeddingStore<TextSegment> seEmbeddingStore;
    private final EmbeddingStore<TextSegment> mpEmbeddingStore;
    private final EmbeddingModel embeddingModel;

    @Service.Inject
    DocsIngestor(Config config,
                 @Service.Named("se-embedding-store") EmbeddingStore<TextSegment> seEmbeddingStore,
                 @Service.Named("mp-embedding-store") EmbeddingStore<TextSegment> mpEmbeddingStore,
                 @Service.Named("all-mini-lm-l6v2-embedding-model") EmbeddingModel embeddingModel) {
        this.config = config;
        this.seEmbeddingStore = seEmbeddingStore;
        this.mpEmbeddingStore = mpEmbeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingestAll() {
        LOGGER.log(INFO, "Starting ingestion ...");
        var ex = Executors.newVirtualThreadPerTaskExecutor();
        allOf(
                runAsync(() -> ingest(HelidonFlavor.MP), ex),
                runAsync(() -> ingest(HelidonFlavor.SE), ex)
        ).join();
    }

    public void ingest(HelidonFlavor flavor) {
        // Get files to process
        var appConfig = config.get("app");
        var root = appConfig.get("helidon-docs-path")
                .as(Path.class)
                .map(p -> p.resolve("src", "main", "asciidoc"))
                .orElseThrow(() -> new ConfigException("Missing app.helidon-repo-path property with path to Helidon project dir"));
        var inclusions = appConfig.get("inclusions").asList(String.class).orElse(Collections.emptyList());
        var exclusions = appConfig.get("exclusions").asList(String.class).orElse(Collections.emptyList());
        var files = FileLister.listFiles(root.resolve(flavor.name().toLowerCase()).toAbsolutePath().toString(), exclusions, inclusions);

        // Process files
        var processor = new AsciiDocPreprocessor();
        var grouper = new ChunkGrouper(1000);
        for (Path path : files) {
            var chunks = processor.extractChunks(path.toFile(), root, flavor);
            var groupedChunks = grouper.groupChunks(chunks);

            // Convert to LangChain4J TextSegments with metadata
            List<TextSegment> segments = new ArrayList<>();
            for (int i = 0; i < groupedChunks.size(); i++) {
                var chunk = groupedChunks.get(i);
                var metadata = new Metadata()
                        .put("source", path.toFile().getAbsolutePath())
                        .put("chunk", String.valueOf(i + 1))
                        .put("type", chunk.type().name())
                        .put("section", chunk.sectionPath());

                segments.add(TextSegment.from(chunk.text(), metadata));
            }

            if (segments.isEmpty()) {
                continue;
            }

            // Embed the segments
            var embeddings = embeddingModel.embedAll(segments);

            if (LOGGER.isLoggable(DEBUG)) {
                // Print segments and metadata
                for (int i = 0; i < segments.size(); i++) {
                    TextSegment segment = segments.get(i);
                    LOGGER.log(DEBUG, "Chunk {0}:\n{0}\n", i + 1, segment.text());
                    LOGGER.log(DEBUG, "Metadata: {0}", segment.metadata());
                    LOGGER.log(DEBUG, "Embedding vector size: {0}", embeddings.content().get(i).vector().length);
                    LOGGER.log(DEBUG, "---");
                }
            }

            switch (flavor) {
            case SE -> seEmbeddingStore.addAll(embeddings.content(), segments);
            case MP -> mpEmbeddingStore.addAll(embeddings.content(), segments);
            }
        }

        LOGGER.log(INFO, "Ingestion done for {0}", flavor.name());
    }
}
