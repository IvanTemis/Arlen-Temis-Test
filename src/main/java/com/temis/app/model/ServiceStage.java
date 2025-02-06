package com.temis.app.model;

import lombok.Getter;

@Getter
public enum ServiceStage {
    UNKNOWN(null),
    ANY(null),
    SOCIETY_IDENTIFICATION("society-identification-agent"),
    DOCUMENT_COLLECTION("document-collection-agent"),
    ORGANIZATIONAL_STRUCTURE("organizational-structure-agent"),
    PAYMENT_COLLECTION("payment-collection-agent"),
    COMPANY_INCORPORATION("company-incorporation-agent"),
    ;

    final String agentId;

    private ServiceStage(String agentId){
        this.agentId = agentId;
    }
}
