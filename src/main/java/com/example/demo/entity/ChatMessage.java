package com.example.demo.entity;

public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    //TODO:设定接收者实现私聊群聊转换？
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public String toString()
    {
        return "sender:"+getSender()+"\n"+
                "type:"+getType()+"\n"+
                "to:"+getTopic()+"\n"+
                "content:"+getContent();
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
