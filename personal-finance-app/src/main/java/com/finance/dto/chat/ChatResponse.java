package com.finance.dto.chat;

import com.finance.dto.coach.AdviceItem;

import java.util.List;

public class ChatResponse {
    private String reply;
    private String conversationId;
    private List<AdviceItem> suggestions;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<AdviceItem> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<AdviceItem> suggestions) {
        this.suggestions = suggestions;
    }
}
