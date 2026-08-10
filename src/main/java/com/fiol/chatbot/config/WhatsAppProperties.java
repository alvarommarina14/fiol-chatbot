package com.fiol.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsAppProperties(
        String accessToken,
        String phoneNumberId,
        String verifyToken,
        String appSecret,
        String apiVersion,
        String graphBaseUrl,
        boolean stripArMobileNine) {
}
