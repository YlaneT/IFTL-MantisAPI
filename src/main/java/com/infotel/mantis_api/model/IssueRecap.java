package com.infotel.mantis_api.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class IssueRecap {
    private String id;
    private String priority;
    private int attachmentCount;
    private String category;
    private String severity;
    private String status;
    private LocalDate updated;
    private String summary;
}
