package com.gg.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

public class ChatMessageTest {

    private final ObjectMapper objectMapper;

    public ChatMessageTest(){
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void CanSerialize(){
        String type = "chatMessage";
        String userId = "potato-user";
        String tableId = "potato-table";
        String message = "i like potatoes";
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setUserId(userId);
        chatMessage.setTableId(tableId);
        chatMessage.setMessage(message);

        JsonNode jsonChatMessage = objectMapper.valueToTree(chatMessage);

        assert type.equals(jsonChatMessage.get("type").asText());
        assert userId.equals(jsonChatMessage.get("userId").asText());
        assert tableId.equals(jsonChatMessage.get("tableId").asText());
        assert message.equals(jsonChatMessage.get("message").asText());
    }

    @Test
    public void CanDeserialize() throws Exception {
        String userId = "potato-user";
        String tableId = "potato-table";
        String message = "i like potatoes";
        String type = "chatMessage";

        ObjectNode jsonObject = objectMapper.createObjectNode();
        jsonObject.put("type", type);
        jsonObject.put("userId", userId);
        jsonObject.put("tableId", tableId);
        jsonObject.put("message", message);

        Message baseMessage = objectMapper.treeToValue(jsonObject, Message.class);

        if (baseMessage instanceof ChatMessage chatMessage){
            assert message.equals(chatMessage.getMessage());
            assert userId.equals(chatMessage.getUserId());
            assert tableId.equals(chatMessage.getTableId());
            assert type.equals(chatMessage.getType());
        }
        else{
            throw new Exception("NOT A THING!");
        }
    }
}
