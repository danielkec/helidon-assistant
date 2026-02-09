package net.dmitrykornilov.helidon.assistant;

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;

@Service.GenerateBinding
public class ApplicationMain {
    static void main(String[] args) {
        LogConfig.configureRuntime();
        // Start Helidon
        ServiceRegistryManager.start(ApplicationBinding.create());
    }
}
