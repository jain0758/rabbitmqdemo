package com.messaging.publish;

import java.util.Map;

public abstract class Message {

    private Map<String, String> messageMetaData;

    public Map<String, String> getMessageMetaData() {
        return messageMetaData;
    }

    public void setMessageMetaData(Map<String, String> messageMetaData) {
        this.messageMetaData = messageMetaData;
    }

}
