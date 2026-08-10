package com.fiol.chatbot.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureVerifierTest {

    private static final String SECRET = "test-secret";
    private static final byte[] BODY = "{\"object\":\"whatsapp_business_account\"}".getBytes(StandardCharsets.UTF_8);

    // Computed independently with: printf '%s' "$BODY" | openssl dgst -sha256 -hmac "test-secret"
    // Hardcoded on purpose so the test does not just re-run the production HMAC code.
    private static final String VALID_SIGNATURE =
            "sha256=60b2e09855b25cf88d59953a4025f83bc48cd03865abe988e974b754c7a285e8";
    private static final String SIGNATURE_FROM_OTHER_SECRET =
            "sha256=a597057ac428a8aef14670e31468251d60371de8c7b5fff6cffca30c98e355dd";

    private final WebhookSignatureVerifier verifier = new WebhookSignatureVerifier();

    @Test
    void givenBodySignedWithAppSecret_whenVerifying_thenAccepts() {
        assertThat(verifier.isValid(BODY, VALID_SIGNATURE, SECRET)).isTrue();
    }

    @Test
    void givenSignatureFromAnotherAppSecret_whenVerifying_thenRejects() {
        assertThat(verifier.isValid(BODY, SIGNATURE_FROM_OTHER_SECRET, SECRET)).isFalse();
    }

    @Test
    void givenTamperedBody_whenVerifying_thenRejects() {
        byte[] tampered = "{\"object\":\"algo_distinto\"}".getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.isValid(tampered, VALID_SIGNATURE, SECRET)).isFalse();
    }

    @Test
    void givenMissingSignatureHeader_whenVerifying_thenRejects() {
        assertThat(verifier.isValid(BODY, null, SECRET)).isFalse();
    }

    @Test
    void givenHeaderWithoutSha256Prefix_whenVerifying_thenRejects() {
        assertThat(verifier.isValid(BODY, "60b2e09855b25cf88d59953a4025f83bc48cd03865abe988e974b754c7a285e8", SECRET))
                .isFalse();
    }

    @Test
    void givenMalformedSignature_whenVerifying_thenRejects() {
        assertThat(verifier.isValid(BODY, "sha256=deadbeef", SECRET)).isFalse();
    }
}
