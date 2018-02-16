package com.messaging.subscribe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cdkglobal.cs.pii.service.dto.FeatureCancelMessage;
import com.cdkglobal.cs.pii.service.dto.FeatureDisablementMessage;
import com.cdkglobal.cs.pii.service.dto.FeatureEnablementMessage;

@Service
public class PisOteEventProcessor {
	
	@Autowired
	private MyMessageProcessor myMessageProcessor;
	
	public void processPisOteEvent(final FeatureEnablementMessage featureEnablementMessage) {
		myMessageProcessor.startOteFlow(featureEnablementMessage.getFeatureEnablement()) ; 
	}
}
