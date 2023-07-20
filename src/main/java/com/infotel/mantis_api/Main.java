package com.infotel.mantis_api;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.service.IssuesServiceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main (String[] args) {
        SpringApplication.run(Main.class, args);
    }
}