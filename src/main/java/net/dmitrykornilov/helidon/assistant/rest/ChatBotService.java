package net.dmitrykornilov.helidon.assistant.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;

import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.JsonObject;
import net.dmitrykornilov.helidon.assistant.ai.ChatAiService;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service.Singleton
public class ChatBotService implements HttpService {

    private final ChatAiService chatAiService;

    @Service.Inject
    public ChatBotService(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.post("/", this::chatWithAssistant);
    }

    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        var json = req.content().as(JsonObject.class);
        var message = json.getString("message");
        var memoryId = json.getString("memory-id");

        var osw = new OutputStreamWriter(res.outputStream(), UTF_8);
        chatAiService.chat(message, memoryId)
                .forEach(chunk -> {
                    try {
                        osw.append(chunk);
                        osw.flush();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
