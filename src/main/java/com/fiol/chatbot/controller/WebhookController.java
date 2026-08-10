package com.fiol.chatbot.controller;

import com.fiol.chatbot.config.WhatsAppProperties;
import com.fiol.chatbot.dto.WebhookEvent;
import com.fiol.chatbot.dto.WebhookEvent.Change;
import com.fiol.chatbot.dto.WebhookEvent.Entry;
import com.fiol.chatbot.dto.WebhookEvent.InboundMessage;
import com.fiol.chatbot.security.WebhookSignatureVerifier;
import com.fiol.chatbot.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class WebhookController {

    private final WhatsAppProperties properties;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    public WebhookController(WhatsAppProperties properties, WebhookSignatureVerifier signatureVerifier,
            ConversationService conversationService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureVerifier = signatureVerifier;
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && properties.verifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            HttpServletRequest request,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) throws IOException {

        byte[] rawBody = request.getInputStream().readAllBytes();

        if (!signatureVerifier.isValid(rawBody, signatureHeader, properties.appSecret())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            WebhookEvent event = objectMapper.readValue(rawBody, WebhookEvent.class);
            processIncoming(event);
        } catch (Exception e) {
            // Always ack with 200 below even on processing errors — Meta disables
            // webhooks that repeatedly time out or return 5xx.
            log.error("Failed to process webhook payload", e);
        }
        return ResponseEntity.ok().build();
    }

    private void processIncoming(WebhookEvent event) {
        for (Entry entry : safe(event.entry())) {
            for (Change change : safe(entry.changes())) {
                if (change.value() == null) {
                    continue;
                }
                for (InboundMessage message : safe(change.value().messages())) {
                    handleMessage(message);
                }
            }
        }
    }

    private void handleMessage(InboundMessage message) {
        String waId = message.from();
        String input = switch (message.type()) {
            case "text" -> message.text() != null ? message.text().body() : null;
            case "interactive" -> message.interactive() != null && message.interactive().buttonReply() != null
                    ? message.interactive().buttonReply().id()
                    : null;
            default -> null;
        };
        if (waId == null || input == null) {
            return;
        }
        // Message bodies are customer content, so only the sender id and message type are
        // logged: enough to trace routing without recording what people actually wrote.
        log.info("Inbound message from wa_id={} type={}", waId, message.type());
        conversationService.handle(waId, input);
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
