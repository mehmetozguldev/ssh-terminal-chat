package com.example.sshterminalchat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final List<Consumer<String>> listeners = new ArrayList<>();

    public void sendMessage(String message) {
        for (Consumer<String> listener : listeners) {
            listener.accept(message);
        }
    }

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }
}
