package com.gg.messages;

public class PlayerAction extends Message {
    protected PlayerAction(String userId, String tableId) {
        super("playerAction", userId, tableId);
    }
}
