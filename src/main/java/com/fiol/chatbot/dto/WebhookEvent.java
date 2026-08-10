package com.fiol.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookEvent(String object, List<Entry> entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(String id, List<Change> changes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Change(String field, ChangeValue value) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangeValue(
            String messagingProduct,
            Metadata metadata,
            List<Contact> contacts,
            List<InboundMessage> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(String displayPhoneNumber, String phoneNumberId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contact(String waId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundMessage(
            String from, String id, String timestamp, String type,
            TextBody text, InteractiveObject interactive) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextBody(String body) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InteractiveObject(String type, ButtonReply buttonReply) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ButtonReply(String id, String title) {
    }
}
