package net.dmitrykornilov.helidon.assistant;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import io.helidon.common.features.api.HelidonFlavor;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;
import io.helidon.service.registry.Services;

import net.dmitrykornilov.helidon.assistant.rag.DocsIngestor;

@Service.GenerateBinding
public class ApplicationMain {
    static void main(String[] args) {
        LogConfig.configureRuntime();
        // Initialize embedding store
        Services.get(DocsIngestor.class).ingestAll();
        // Start Helidon
        ServiceRegistryManager.start(ApplicationBinding.create());
    }
}
