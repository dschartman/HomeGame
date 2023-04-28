package com.gg.messages;

public class ChatMessage extends Message{
    private String playerId;
    private String tableId;
    private String message;

    public ChatMessage(String userId, String tableId) {
        super("chatMessage", userId, tableId);
    }

    public ChatMessage(String userId, String tableId, String message) {
        super("chatMessage", userId, tableId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

