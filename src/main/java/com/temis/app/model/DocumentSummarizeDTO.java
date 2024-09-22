package com.temis.app.model;

import java.sql.Date;

public class DocumentSummarizeDTO {
    private Long id;
    private String summarize;
    private Date creationDate;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSummarize() {
        return summarize;
    }
    public void setSummarize(String summarize) {
        this.summarize = summarize;
    }
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    
}
