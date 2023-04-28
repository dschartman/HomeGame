package com.gg.messages;

public abstract class Message {
    protected String messageType;
    protected String userId;
    protected String tableId;

    protected Message(String messageType) {
        this.messageType = messageType;
    }

    protected Message(String messageType, String userId, String tableId) {
        this.messageType = messageType;
        this.userId = userId;
        this.tableId = tableId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
}
