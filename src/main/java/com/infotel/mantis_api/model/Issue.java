package com.infotel.mantis_api.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
public class Issue {
    
    @Id
    @GeneratedValue
    private String              id;
    private String              project;
    private String              category;
    private String              viewStatus;
    private LocalDateTime       submitted;
    private LocalDateTime       lastUpdated;
    private String              reporter;
    private String              assigned;
    private String              priority;
    private String              severity;
    private String              reproducibility;
    private String              status;
    private String              resolution;
    private String              platform;
    private String              os;
    private String              osVersion;
    private String              summary;
    private String              description;
    private List<String>        tags;
    private Integer             noteCount;
    private Integer             attachmentCount;
    // Optional fields /!\ appear before tags
    private String              stepsToReproduce;
    private String              additionalInformation;
    private Map<String, String> customFields;
    
    
    public Issue () {
        tags = new ArrayList<>();
        customFields = new HashMap<>();
    }
    
    public void setSummary (String summary) {
        String summaryStart = this.id + ": ";
        if (summary.startsWith(summaryStart)) {
            summary = summary.substring(summaryStart.length());
        }
        this.summary = summary;
    }
}
