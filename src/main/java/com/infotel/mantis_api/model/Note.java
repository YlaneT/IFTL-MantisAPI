package com.infotel.mantis_api.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Data
@Entity
public class Note {

    @GeneratedValue
    private String note;
    private String viewStatus;
}
