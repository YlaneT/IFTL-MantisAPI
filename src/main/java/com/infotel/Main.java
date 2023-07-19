package com.infotel;

import com.infotel.model.Issue;
import com.infotel.service.IssuesServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        IssuesServiceImpl service = new IssuesServiceImpl();
        
        List<Issue> issues = new ArrayList<>();
        
        issues.add(service.searchIssue(1)); // status : acknowledged
        issues.add(service.searchIssue(2)); // Steps to reproduce
        issues.add(service.searchIssue(3)); // Tags
        issues.add(service.searchIssue(4)); // Custom field / Priority none
        issues.add(service.searchIssue(5)); // minimal & unassigned
        issues.add(service.searchIssue(6)); // private & unassigned
        issues.add(service.searchIssue(7)); // maximal
    }
}