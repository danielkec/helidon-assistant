package net.dmitrykornilov.helidon.assistant.rag;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;

import io.helidon.config.Config;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

import static java.lang.System.Logger.Level.DEBUG;

@Service.Singleton
public class DocsIngestor {
    private static final System.Logger LOGGER = System.getLogger(DocsIngestor.class.getName());

    private final Config config;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Service.Inject
    DocsIngestor(Config config,
                 @Service.Named("oracle") EmbeddingStore<TextSegment> embeddingStore,
                 EmbeddingModel embeddingModel) {
        this.config = config;
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingest(BiConsumer<Integer, Integer> progressUpdater) {
        // Get files to process
        var appConfig = config.get("app");
        var root = appConfig.get("root").asString().orElseThrow();
        var inclusions = appConfig.get("inclusions").asList(String.class).orElse(Collections.emptyList());
        var exclusions = appConfig.get("exclusions").asList(String.class).orElse(Collections.emptyList());
        var files = FileLister.listFiles(root, exclusions, inclusions);

        // First, send max progress bar value
        progressUpdater.accept(0, files.size());

        // Process files
        var processor = new AsciiDocPreprocessor();
        var grouper = new ChunkGrouper(1000);

        LongAdder y = new LongAdder();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var file : files) {
                executor.submit(() -> {
                    ingestFile(file.toFile(), processor, grouper);
                    y.increment();
                    progressUpdater.accept(y.intValue(), files.size());
                });
            }
        }
    }

    void ingestFile(File file, AsciiDocPreprocessor processor, ChunkGrouper grouper) {
        var chunks = processor.extractChunks(file);
        var groupedChunks = grouper.groupChunks(chunks);

        // Convert to LangChain4j TextSegments with metadata
        List<TextSegment> segments = new ArrayList<>();
        for (int i = 0; i < groupedChunks.size(); i++) {
            var chunk = groupedChunks.get(i);
            var metadata = new Metadata()
                    .put("source", file.getAbsolutePath())
                    .put("chunk", String.valueOf(i + 1))
                    .put("type", chunk.type().name())
                    .put("section", chunk.sectionPath());

            segments.add(TextSegment.from(chunk.text(), metadata));
        }

        // Embed the segments
        var embeddings = embeddingModel.embedAll(segments);

        // Print segments and metadata
        for (int i = 0; LOGGER.isLoggable(DEBUG) && i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            LOGGER.log(DEBUG, "Chunk {0}: \n {1} \n", i - 1, segment.text());
            LOGGER.log(DEBUG, "Metadata: {0}", segment.metadata());
            LOGGER.log(DEBUG, "Embedding vector size:  {0}", embeddings.content().get(i).vector().length);
            LOGGER.log(DEBUG, "---");
        }

        embeddingStore.addAll(embeddings.content(), segments);
    }

    public void clear() {
        embeddingStore.removeAll();
    }
}
