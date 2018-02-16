package com.messaging.subscribe;

import lombok.Data;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@Data
@ToString(includeFieldNames=true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "featureEnablement")
public class FeatureEnablementDto {
	 private String productId;
	    private String productCode;
	    private String productVersionId;
	    private String versionIdentifier;
	    private String featureId;
	    private String featureCode;
	    private String featureInstanceId;
	    private String storeId;
	    private String orgUnitId;
	    private String departmentId;
	    private String rollCalled;
	    private String rollCalledDate;
	    private String provisioned;
	    private String provisionedDate;

}
