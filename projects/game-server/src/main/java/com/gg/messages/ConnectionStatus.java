package com.gg.messages;

public class ConnectionStatus extends Message {
    private String message;

    public ConnectionStatus(String userId, String tableId, String message) {
        super("connectionStatus", userId, tableId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
