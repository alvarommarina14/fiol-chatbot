package com.fiol.chatbot.service;

import com.fiol.chatbot.client.CatalogClient;
import com.fiol.chatbot.client.WhatsAppClient;
import com.fiol.chatbot.dto.Product;
import com.fiol.chatbot.dto.ReplyButtonSpec;
import com.fiol.chatbot.model.ConversationState;
import com.fiol.chatbot.repository.ConversationStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    static final String MENU_PRICE = "MENU_PRICE";
    static final String MENU_FAQ = "MENU_FAQ";
    static final String MENU_HUMAN = "MENU_HUMAN";

    private static final String GREETING = "¡Hola! Bienvenido a Fiol. ¿En qué te puedo ayudar?";
    private static final String MAIN_MENU_PROMPT = "¿En qué te puedo ayudar?";
    private static final String HUMAN_HANDOFF_MESSAGE = "Un momento, alguien del equipo te va a responder por acá.";
    private static final String ASK_PRODUCT_MESSAGE = "Escribí el nombre o código del producto que buscás.";
    private static final String NOT_FOUND_MESSAGE =
            "No encontré ningún producto con eso. Probá con otro nombre o código, o escribí \"menu\" para volver.";

    private final ConversationStateRepository stateRepository;
    private final WhatsAppClient whatsAppClient;
    private final CatalogClient catalogClient;
    private final FaqService faqService;

    public ConversationService(ConversationStateRepository stateRepository, WhatsAppClient whatsAppClient,
            CatalogClient catalogClient, FaqService faqService) {
        this.stateRepository = stateRepository;
        this.whatsAppClient = whatsAppClient;
        this.catalogClient = catalogClient;
        this.faqService = faqService;
    }

    public void handle(String waId, String rawInput) {
        String input = rawInput == null ? "" : rawInput.trim();
        ConversationState state = stateRepository.getState(waId);
        switch (state) {
            case MAIN_MENU -> routeMainMenu(waId, input);
            case AWAITING_PRODUCT_QUERY -> routeProductQuery(waId, input);
        }
    }

    private void routeMainMenu(String waId, String input) {
        switch (input) {
            case MENU_PRICE -> {
                whatsAppClient.sendText(waId, ASK_PRODUCT_MESSAGE);
                stateRepository.setState(waId, ConversationState.AWAITING_PRODUCT_QUERY);
            }
            case MENU_FAQ -> {
                whatsAppClient.sendText(waId, faqService.renderAll());
                sendMainMenu(waId);
            }
            case MENU_HUMAN -> {
                whatsAppClient.sendText(waId, HUMAN_HANDOFF_MESSAGE);
                sendMainMenu(waId);
            }
            default -> {
                whatsAppClient.sendText(waId, GREETING);
                sendMainMenu(waId);
            }
        }
    }

    private void routeProductQuery(String waId, String input) {
        if ("menu".equalsIgnoreCase(input)) {
            stateRepository.setState(waId, ConversationState.MAIN_MENU);
            sendMainMenu(waId);
            return;
        }

        List<Product> matches = catalogClient.search(input);
        if (matches.isEmpty()) {
            whatsAppClient.sendText(waId, NOT_FOUND_MESSAGE);
            return;
        }

        whatsAppClient.sendText(waId, renderResults(matches));
        stateRepository.setState(waId, ConversationState.MAIN_MENU);
        sendMainMenu(waId);
    }

    private void sendMainMenu(String waId) {
        whatsAppClient.sendReplyButtons(waId, MAIN_MENU_PROMPT, List.of(
                new ReplyButtonSpec(MENU_PRICE, "Consultar precio"),
                new ReplyButtonSpec(MENU_FAQ, "Preguntas frecuentes"),
                new ReplyButtonSpec(MENU_HUMAN, "Hablar con humano")));
    }

    private String renderResults(List<Product> matches) {
        StringBuilder sb = new StringBuilder("Encontré esto:\n\n");
        for (Product product : matches) {
            sb.append("• ").append(product.name())
                    .append(" [").append(product.code()).append("] — $")
                    .append(product.priceArs().toPlainString()).append("\n");
        }
        return sb.toString().trim();
    }
}
