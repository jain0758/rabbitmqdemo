package com.messaging.subscribe;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames=true)
public class MessageMetaData {
	 private String eventOriginator;
	 private String messageVersionId;
	 private String eventName;
	 private String messageId;
	 private String routingKey;
}
