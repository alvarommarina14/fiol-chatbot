package com.fiol.chatbot.dto;

public record TextMessageRequest(
        String messagingProduct, String recipientType, String to, String type, TextContent text) {

    public record TextContent(boolean previewUrl, String body) {
    }

    public static TextMessageRequest of(String to, String body) {
        return new TextMessageRequest("whatsapp", "individual", to, "text", new TextContent(false, body));
    }
}
