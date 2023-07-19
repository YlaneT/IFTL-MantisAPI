package com.infotel.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Issue {
    @Id
    @GeneratedValue
    private String        id;
    private String        project;
    private String        category;
    private LocalDateTime submitted;
    private LocalDateTime lastUpdated;
    private String        reporter;
    private String        assigned;
    private String        priority;
    private String        status;
    private String        severity;
    private String        reproducibility;
    private String        summary;
    private String        description;
    private List<String>  tags; // id, name, description
    // Optional fields /!\ appear before tags
    private String        stepsToReproduce;
    private String        additionalInformation;
    // TODO: Custom fields
    
    
    public Issue () {
        tags = new ArrayList<>();
    }
    
    public void setSummary (String summary) {
        String summaryStart = this.id + ": ";
        if (summary.startsWith(summaryStart)) {
             summary = summary.substring(summaryStart.length());
        }
        this.summary = summary;
    }
}
