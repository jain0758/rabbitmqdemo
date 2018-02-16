package com.messaging.publish;

public enum MessageProperty
{
	EVENT_NAME("event_name"),EVENT_ORIGINATOR("event_originator"), VERSION("version"), MESSAGE_ID("message_id"), ROUTING_KEY("routing_key");
	
	private String value;

	private MessageProperty(String s)
	{
		this.value = s;
	}

	public String value()
	{
		return value;
	}
}
