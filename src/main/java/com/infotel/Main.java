package com.infotel;

import com.infotel.model.Issue;
import com.infotel.service.IssuesServiceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main (String[] args) {
        SpringApplication.run(Main.class, args);
    }
    
    private static void actions () {
        
        IssuesServiceImpl service = new IssuesServiceImpl();
        
        List<Issue> issues = new ArrayList<>();
        
        issues.add(service.searchIssue(1)); // status : acknowledged
        issues.add(service.searchIssue(2)); // Steps to reproduce
        issues.add(service.searchIssue(3)); // Tags
        issues.add(service.searchIssue(4)); // Custom field / Priority none
        issues.add(service.searchIssue(5)); // minimal & unassigned
        issues.add(service.searchIssue(6)); // private & unassigned / steps to reproduce & additional information
        issues.add(service.searchIssue(7)); // maximal
    }
}