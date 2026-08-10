package com.fiol.chatbot.dto;

import java.util.List;

public record InteractiveMessageRequest(
        String messagingProduct, String recipientType, String to, String type, InteractiveContent interactive) {

    public record InteractiveContent(String type, InteractiveBody body, InteractiveAction action) {
    }

    public record InteractiveBody(String text) {
    }

    public record InteractiveAction(List<ReplyButton> buttons) {
    }

    public record ReplyButton(String type, ButtonPayload reply) {
    }

    public record ButtonPayload(String id, String title) {
    }

    public static InteractiveMessageRequest buttons(String to, String bodyText, List<ReplyButton> buttons) {
        return new InteractiveMessageRequest("whatsapp", "individual", to, "interactive",
                new InteractiveContent("button", new InteractiveBody(bodyText), new InteractiveAction(buttons)));
    }
}
