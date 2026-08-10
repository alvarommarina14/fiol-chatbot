package com.fiol.chatbot.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FaqServiceTest {

    private final FaqService faqService = new FaqService();

    @Test
    void givenFaqEntries_whenRendering_thenListsAllQuestionsNumberedWithAnswers() {
        String rendered = faqService.renderAll();

        assertThat(rendered)
                .startsWith("Preguntas frecuentes:")
                .contains("1) ¿Cuáles son los horarios de atención?")
                .contains("2) ¿Hacen envíos?")
                .contains("3) ¿Qué medios de pago aceptan?")
                .contains("4) ¿Tienen local físico?")
                .contains("Atendemos de lunes a viernes");
    }

    @Test
    void givenFaqEntries_whenRendering_thenHasNoTrailingWhitespace() {
        assertThat(faqService.renderAll()).isEqualTo(faqService.renderAll().strip());
    }
}
