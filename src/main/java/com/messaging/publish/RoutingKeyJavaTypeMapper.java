package com.messaging.publish;

import java.util.Map;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;

public class RoutingKeyJavaTypeMapper extends DefaultJackson2JavaTypeMapper {

    private Map<String, String> routingKeyMapping;

    public void setRoutingKeyMapping(Map<String, String> routingKeyMapping) {
        this.routingKeyMapping = routingKeyMapping;
    }

    @Override
    protected String retrieveHeader(MessageProperties properties, String headerName) {
        if (null != properties.getReceivedRoutingKey()
                && routingKeyMapping.containsKey(properties.getReceivedRoutingKey())) {
            return routingKeyMapping.get(properties.getReceivedRoutingKey());
        }
        return super.retrieveHeader(properties, headerName);
    }

}
