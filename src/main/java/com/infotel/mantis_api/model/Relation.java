package com.infotel.mantis_api.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Data
public class Relation {
    String type;
    Integer issue_id;
    String status;
    String assigned;
    String project;
    String summary;
}