package com.gg.messages;

public class LeaveTable extends Message {
    protected LeaveTable(String userId, String tableId) {
        super("leaveTable", userId, tableId);
    }
}
