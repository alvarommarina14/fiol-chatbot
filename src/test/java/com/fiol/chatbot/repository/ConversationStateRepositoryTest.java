package com.fiol.chatbot.repository;

import com.fiol.chatbot.model.ConversationState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationStateRepositoryTest {

    private final ConversationStateRepository repository = new ConversationStateRepository();

    @Test
    void givenUnknownNumber_whenGettingState_thenDefaultsToMainMenu() {
        assertThat(repository.getState("5491100000000")).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenStoredState_whenGettingState_thenReturnsIt() {
        repository.setState("549110000001", ConversationState.AWAITING_PRODUCT_QUERY);

        assertThat(repository.getState("549110000001")).isEqualTo(ConversationState.AWAITING_PRODUCT_QUERY);
    }

    @Test
    void givenExistingState_whenSettingNewState_thenOverwritesIt() {
        repository.setState("549110000002", ConversationState.AWAITING_PRODUCT_QUERY);
        repository.setState("549110000002", ConversationState.MAIN_MENU);

        assertThat(repository.getState("549110000002")).isEqualTo(ConversationState.MAIN_MENU);
    }

    @Test
    void givenStateStoredForOneNumber_whenGettingAnotherNumber_thenReturnsDefault() {
        repository.setState("549110000003", ConversationState.AWAITING_PRODUCT_QUERY);

        assertThat(repository.getState("549110000004")).isEqualTo(ConversationState.MAIN_MENU);
    }
}
