package net.dmitrykornilov.helidon.assistant.ai;

import java.util.stream.Stream;
import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@Ai.Service
@Ai.ChatMemoryProvider(AssistantMemoryProvider.NAME)
public interface ChatAiService {

    @SystemMessage("""
            You are Frank, a helpful Helidon expert.
            
            Only answer questions related to Helidon and its components. If a question is not relevant to Helidon, 
            politely decline.
            """)
    Stream<String> chat(@UserMessage String question, @MemoryId String memoryId);
}