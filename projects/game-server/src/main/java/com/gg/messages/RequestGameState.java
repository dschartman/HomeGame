package com.gg.messages;

public class RequestGameState extends Message {
    protected RequestGameState(String userId, String tableId) {
        super("requestGameState", userId, tableId);
    }
}
