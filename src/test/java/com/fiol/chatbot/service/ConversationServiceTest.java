package com.fiol.chatbot.service;

import com.fiol.chatbot.client.CatalogClient;
import com.fiol.chatbot.client.WhatsAppClient;
import com.fiol.chatbot.dto.Product;
import com.fiol.chatbot.dto.ReplyButtonSpec;
import com.fiol.chatbot.model.ConversationState;
import com.fiol.chatbot.repository.ConversationStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    private static final String WA_ID = "5491122334455";

    @Mock
    private WhatsAppClient whatsAppClient;

    @Mock
    private CatalogClient catalogClient;

    private ConversationStateRepository stateRepository;
    private ConversationService service;

    @BeforeEach
    void setUp() {
        // Real repository and FaqService: both are deterministic and side-effect free,
        // so asserting on real state is stronger than verifying mock calls.
        stateRepository = new ConversationStateRepository();
        service = new ConversationService(stateRepository, whatsAppClient, catalogClient, new FaqService());
    }

    @Test
    void givenNewConversation_whenUnrecognizedMessage_thenSendsGreetingAndMenu() {
        service.handle(WA_ID, "hola");

        assertThat(capturedText()).contains("Bienvenido");
        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenMainMenu_whenPriceButtonTapped_thenAsksForProductAndAwaitsQuery() {
        service.handle(WA_ID, ConversationService.MENU_PRICE);

        assertThat(capturedText()).contains("nombre o código");
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.AWAITING_PRODUCT_QUERY);
        verify(whatsAppClient, never()).sendReplyButtons(anyString(), anyString(), anyList());
    }

    @Test
    void givenMainMenu_whenFaqButtonTapped_thenSendsFaqAndShowsMenuAgain() {
        service.handle(WA_ID, ConversationService.MENU_FAQ);

        assertThat(capturedText()).contains("Preguntas frecuentes", "horarios de atención");
        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenMainMenu_whenHumanButtonTapped_thenSendsHandoffMessageAndShowsMenuAgain() {
        service.handle(WA_ID, ConversationService.MENU_HUMAN);

        assertThat(capturedText()).contains("alguien del equipo");
        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenAwaitingProductQuery_whenCatalogHasMatches_thenSendsNameCodeAndPrice() {
        stateRepository.setState(WA_ID, ConversationState.AWAITING_PRODUCT_QUERY);
        when(catalogClient.search("cuerdas")).thenReturn(List.of(
                new Product("V002", "Cuerdas de guitarra criolla", new BigDecimal("8200.00"))));

        service.handle(WA_ID, "cuerdas");

        assertThat(capturedText()).contains("Cuerdas de guitarra criolla", "V002", "8200.00");
        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenAwaitingProductQuery_whenCatalogHasNoMatches_thenKeepsAwaitingSoUserCanRetry() {
        stateRepository.setState(WA_ID, ConversationState.AWAITING_PRODUCT_QUERY);
        when(catalogClient.search("saxofon")).thenReturn(List.of());

        service.handle(WA_ID, "saxofon");

        assertThat(capturedText()).contains("No encontré");
        verify(whatsAppClient, never()).sendReplyButtons(anyString(), anyString(), anyList());
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.AWAITING_PRODUCT_QUERY);
    }

    @Test
    void givenAwaitingProductQuery_whenUserTypesMenu_thenReturnsToMenuWithoutSearching() {
        stateRepository.setState(WA_ID, ConversationState.AWAITING_PRODUCT_QUERY);

        service.handle(WA_ID, "MENU");

        verifyNoInteractions(catalogClient);
        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenAwaitingProductQuery_whenQueryHasSurroundingSpaces_thenSearchesTrimmedQuery() {
        stateRepository.setState(WA_ID, ConversationState.AWAITING_PRODUCT_QUERY);
        when(catalogClient.search("cuerdas")).thenReturn(List.of());

        service.handle(WA_ID, "  cuerdas  ");

        verify(catalogClient).search("cuerdas");
    }

    @Test
    void givenMainMenu_whenInputIsNull_thenShowsMenuWithoutFailing() {
        service.handle(WA_ID, null);

        assertMenuButtonsSent();
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenTwoDifferentNumbers_whenOneAdvances_thenTheOtherKeepsItsOwnState() {
        String otherNumber = "5491199887766";

        service.handle(WA_ID, ConversationService.MENU_PRICE);

        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.AWAITING_PRODUCT_QUERY);
        assertThat(stateRepository.getState(otherNumber)).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenMainMenu_whenMenuIsSent_thenButtonTitlesFitWhatsappLimit() {
        service.handle(WA_ID, "hola");

        // WhatsApp rejects the whole interactive message if a reply button title
        // exceeds 20 characters, so this guards against an easy copy regression.
        assertThat(capturedButtons())
                .extracting(ReplyButtonSpec::title)
                .allSatisfy(title -> assertThat(title).hasSizeLessThanOrEqualTo(20));
    }

    @Test
    void givenNewConversation_whenGreetingThenPriceButtonThenCode_thenWalksTheWholeFlow() {
        when(catalogClient.search("V002")).thenReturn(List.of(
                new Product("V002", "Cuerdas de guitarra criolla", new BigDecimal("8200.00"))));

        service.handle(WA_ID, "hola");
        service.handle(WA_ID, ConversationService.MENU_PRICE);
        service.handle(WA_ID, "V002");

        ArgumentCaptor<String> texts = ArgumentCaptor.captor();
        verify(whatsAppClient, times(3)).sendText(eq(WA_ID), texts.capture());
        assertThat(texts.getAllValues().get(0)).contains("Bienvenido");
        assertThat(texts.getAllValues().get(1)).contains("nombre o código");
        assertThat(texts.getAllValues().get(2)).contains("Cuerdas de guitarra criolla");

        // Menu shown after the greeting and again after the results, but not while
        // the bot was waiting for the product query.
        verify(whatsAppClient, times(2)).sendReplyButtons(eq(WA_ID), anyString(), anyList());
        assertThat(stateRepository.getState(WA_ID)).isEqualTo(ConversationState.MAIN_MENU);
    }

    private String capturedText() {
        ArgumentCaptor<String> captor = ArgumentCaptor.captor();
        verify(whatsAppClient).sendText(eq(WA_ID), captor.capture());
        return captor.getValue();
    }

    private List<ReplyButtonSpec> capturedButtons() {
        ArgumentCaptor<List<ReplyButtonSpec>> captor = ArgumentCaptor.captor();
        verify(whatsAppClient).sendReplyButtons(eq(WA_ID), anyString(), captor.capture());
        return captor.getValue();
    }

    private void assertMenuButtonsSent() {
        assertThat(capturedButtons())
                .extracting(ReplyButtonSpec::id)
                .containsExactly(
                        ConversationService.MENU_PRICE,
                        ConversationService.MENU_FAQ,
                        ConversationService.MENU_HUMAN);
    }
}
