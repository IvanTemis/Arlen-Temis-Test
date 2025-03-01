package com.temis.app.agent;

import com.google.cloud.vertexai.api.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.utils.VertexAIUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DocumentClassifierAgent {

    private final AgentManager agentManager;

    @Autowired
    public DocumentClassifierAgent(AgentManager agentManager)  {
        this.agentManager = agentManager;
    }


    public String Classify(String gsUrl, String mimeType, String context) throws Exception {
        var content = VertexAIUtils.ContentWithDocument("Clasifica este documento\n" + context, gsUrl, mimeType);

        return agentManager.sendSimpleMessageToAgent("document-classifier-agent", content);
    }
}
