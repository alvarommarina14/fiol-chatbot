package com.fiol.chatbot.client;

import com.fiol.chatbot.config.WhatsAppProperties;
import com.fiol.chatbot.dto.InteractiveMessageRequest;
import com.fiol.chatbot.dto.InteractiveMessageRequest.ButtonPayload;
import com.fiol.chatbot.dto.InteractiveMessageRequest.ReplyButton;
import com.fiol.chatbot.dto.ReplyButtonSpec;
import com.fiol.chatbot.dto.TextMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Component
public class WhatsAppClient {

    private final RestClient restClient;
    private final WhatsAppProperties properties;

    public WhatsAppClient(RestClient whatsAppRestClient, WhatsAppProperties properties) {
        this.restClient = whatsAppRestClient;
        this.properties = properties;
    }

    public void sendText(String toWaId, String body) {
        send(TextMessageRequest.of(toWaId, body));
    }

    public void sendReplyButtons(String toWaId, String bodyText, List<ReplyButtonSpec> buttons) {
        List<ReplyButton> replyButtons = buttons.stream()
                .map(b -> new ReplyButton("reply", new ButtonPayload(b.id(), b.title())))
                .toList();
        send(InteractiveMessageRequest.buttons(toWaId, bodyText, replyButtons));
    }

    private void send(Object payload) {
        try {
            restClient.post()
                    .uri("/{phoneNumberId}/messages", properties.phoneNumberId())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.error("WhatsApp API call failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
