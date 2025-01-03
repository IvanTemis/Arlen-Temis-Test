package com.temis.app.service;

public interface DraftEmailService {

    public void sendDraftByEmail(String draftText, String emailAddress) throws Exception;
}