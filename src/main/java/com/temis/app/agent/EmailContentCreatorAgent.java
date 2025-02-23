package com.temis.app.agent;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.manager.AgentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EmailContentCreatorAgent {

    private final AgentManager agentManager;

    @Autowired
    public EmailContentCreatorAgent(AgentManager agentManager)  {
        this.agentManager = agentManager;
    }

    public String CreateEmailContent(String content) throws Exception {
        return agentManager.sendSimpleMessageToAgent("email-content-creator-agent", ContentMaker.fromMultiModalData(content));
    }
}
