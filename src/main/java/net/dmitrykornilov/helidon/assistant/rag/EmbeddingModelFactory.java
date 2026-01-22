package net.dmitrykornilov.helidon.assistant.rag;

import java.util.function.Supplier;

import io.helidon.service.registry.Service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;

/**
 * Creates EmbeddingModel as a service
 */
@Service.Singleton
public class EmbeddingModelFactory implements Supplier<EmbeddingModel> {
    @Override
    public EmbeddingModel get() {
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

}
