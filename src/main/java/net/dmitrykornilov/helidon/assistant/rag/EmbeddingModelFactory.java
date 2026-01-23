package net.dmitrykornilov.helidon.assistant.rag;

import java.util.function.Supplier;

import io.helidon.service.registry.Service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;

/**
 * Creates EmbeddingModel as a service
 */
@Service.Singleton
@Service.Named("all-mini-lm-l6v2-embedding-model")
// TODO: add providers for lc4j local models
public class EmbeddingModelFactory implements Supplier<EmbeddingModel> {
    @Override
    public EmbeddingModel get() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }
}
