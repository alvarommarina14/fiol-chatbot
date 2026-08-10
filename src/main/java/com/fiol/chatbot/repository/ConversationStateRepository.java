package com.fiol.chatbot.repository;

import com.fiol.chatbot.model.ConversationState;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ConversationStateRepository {

    private final Map<String, ConversationState> states = new ConcurrentHashMap<>();

    public ConversationState getState(String waId) {
        return states.getOrDefault(waId, ConversationState.MAIN_MENU);
    }

    public void setState(String waId, ConversationState state) {
        states.put(waId, state);
    }
}
