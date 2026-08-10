package com.fiol.chatbot.client;

import com.fiol.chatbot.config.WhatsAppProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientNormalizerTest {

    private static RecipientNormalizer normalizerWith(boolean stripArMobileNine) {
        return new RecipientNormalizer(new WhatsAppProperties(
                "token", "phone-number-id", "verify-token", "app-secret",
                "v21.0", "https://graph.facebook.com", stripArMobileNine));
    }

    private final RecipientNormalizer enabled = normalizerWith(true);
    private final RecipientNormalizer disabled = normalizerWith(false);

    @Test
    void givenDisabled_whenNormalizingArgentineMobile_thenKeepsTheNine() {
        assertThat(disabled.normalize("5493416512982")).isEqualTo("5493416512982");
    }

    @Test
    void givenEnabled_whenNormalizingArgentineMobile_thenDropsTheNine() {
        assertThat(enabled.normalize("5493416512982")).isEqualTo("543416512982");
    }

    @Test
    void givenEnabled_whenNumberIsNotArgentine_thenLeavesItUntouched() {
        // Brazilian mobile: starts with 55, so the 549 prefix must not match.
        assertThat(enabled.normalize("5511987654321")).isEqualTo("5511987654321");
    }

    @Test
    void givenEnabled_whenArgentineNumberIsNotAMobile_thenLeavesItUntouched() {
        // Landline, 12 digits: no 9 to drop even though it starts with 54.
        assertThat(enabled.normalize("543416512982")).isEqualTo("543416512982");
    }

    @Test
    void givenEnabled_whenPrefixMatchesButLengthDoesNot_thenLeavesItUntouched() {
        // Guards against mangling ids that merely happen to start with 549.
        assertThat(enabled.normalize("54934165129")).isEqualTo("54934165129");
        assertThat(enabled.normalize("54934165129821")).isEqualTo("54934165129821");
    }

    @Test
    void givenNullRecipient_whenNormalizing_thenReturnsNull() {
        assertThat(enabled.normalize(null)).isNull();
        assertThat(disabled.normalize(null)).isNull();
    }
}
