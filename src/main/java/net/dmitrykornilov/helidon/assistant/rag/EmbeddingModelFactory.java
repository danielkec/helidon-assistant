package net.dmitrykornilov.helidon.assistant.rag;

import java.util.function.Supplier;

import io.helidon.service.registry.Service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;

/**
 * Creates EmbeddingModel as a service
 */
@Service.Singleton
public class EmbeddingModelFactory implements Supplier<EmbeddingModel> {
    @Override
    public EmbeddingModel get() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }
}
