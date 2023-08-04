package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.*;
import com.infotel.mantis_api.model.Issue;

import java.util.List;

public interface IssuesService {
    // Get an issue
    Issue searchIssue (int id) throws IssueNotFoundException, AccessDenied;
    Issue searchIssue (int id, List<String> selectValues) throws IssueNotFoundException, FieldNotFoundException, AccessDenied;
    
    // Get all issues
    List<Issue> searchAllIssues (int pageSize, int page, List<String> selectValues, int projectId) throws FieldNotFoundException, ProjectNotFoundException;
    
    // Edit issue
    String editIssue (int id, Issue issue) throws IssueNotFoundException, AccessDenied;
    
    // Add note to issue
    void addNote(int id, String note);

    // Create an issue
    String createIssue (String project, String category, String reproducibility, String severity,
        String priority, String platform, String os,
        String osVersion, String assigned, String summary, String description,
        String stepsToReproduce, String additionalInformation) throws FieldNotFoundException, AccessDenied, ProjectNotFoundException;
}
