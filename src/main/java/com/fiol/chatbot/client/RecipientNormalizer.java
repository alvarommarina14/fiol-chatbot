package com.fiol.chatbot.client;

import com.fiol.chatbot.config.WhatsAppProperties;
import org.springframework.stereotype.Component;

/**
 * Adjusts recipient ids for Argentine mobile numbers, which carry a 9 between the country code
 * and the area code.
 *
 * <p>WhatsApp reports inbound senders as {@code 549XXXXXXXXXX}, and replying to that id is
 * correct in production. Development phone numbers, however, only deliver to recipients on a
 * test allow list, and that list stores Argentine mobiles without the 9 — Meta's console
 * rewrites a number entered as {@code +54 9 341 ...} into the domestic {@code +54 341 15 ...}
 * form, so the wa_id itself can never be registered. Sending to such an id fails with error
 * 131030 even though the number is listed.
 *
 * <p>Dropping the 9 works around that, so it is enabled only where the allow list exists. The
 * default is off: production has no allow list, and the unmodified wa_id is the right recipient.
 */
@Component
public class RecipientNormalizer {

    private static final String AR_MOBILE_PREFIX = "549";
    private static final int AR_MOBILE_LENGTH = 13;

    private final WhatsAppProperties properties;

    public RecipientNormalizer(WhatsAppProperties properties) {
        this.properties = properties;
    }

    public String normalize(String waId) {
        if (!properties.stripArMobileNine() || waId == null) {
            return waId;
        }
        if (waId.length() == AR_MOBILE_LENGTH && waId.startsWith(AR_MOBILE_PREFIX)) {
            return "54" + waId.substring(AR_MOBILE_PREFIX.length());
        }
        return waId;
    }
}
