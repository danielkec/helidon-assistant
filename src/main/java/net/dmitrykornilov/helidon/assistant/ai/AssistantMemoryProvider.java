package net.dmitrykornilov.helidon.assistant.ai;

import java.util.function.Supplier;

import io.helidon.service.registry.Service;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

@Service.Singleton
@Service.Named(AssistantMemoryProvider.NAME)
public class AssistantMemoryProvider implements Supplier<ChatMemoryProvider> {

    static final String NAME = "assistantMemory";

    @Override
    public ChatMemoryProvider get() {
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(10)
                .id(memoryId)
                .chatMemoryStore(new InMemoryChatMemoryStore()).build();
    }
}