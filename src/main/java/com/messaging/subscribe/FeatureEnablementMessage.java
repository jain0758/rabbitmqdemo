package com.messaging.subscribe;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames=true)
public class FeatureEnablementMessage {
	private MessageMetaData messageMetaData;
	private FeatureEnablementDto featureEnablement;
}
