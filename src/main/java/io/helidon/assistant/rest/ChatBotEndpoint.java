package io.helidon.assistant.rest;

import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.JsonObject;
import io.helidon.assistant.ai.HelidonExpertAgent;

import static io.helidon.common.media.type.MediaTypes.APPLICATION_JSON_VALUE;

@RestServer.Endpoint
@Http.Path("/chat")
@Service.Singleton
class ChatBotEndpoint {

    private final HelidonExpertAgent agent;

    @Service.Inject
    ChatBotEndpoint(HelidonExpertAgent agent) {
        this.agent = agent;
    }

    @Http.GET
    @Http.Produces(APPLICATION_JSON_VALUE)
    JsonObject test() {
        return agent.chat("How to create query with Helidon SE data repository? Show me example please.", "");
    }

    @Http.POST
    @Http.Produces(APPLICATION_JSON_VALUE)
    JsonObject chatWithAssistant(ServerRequest req, ServerResponse res) {
        var json = req.content().as(JsonObject.class);
        var message = json.getString("message");
        var summary = json.getString("summary");
        return agent.chat(message, summary);
    }
}
