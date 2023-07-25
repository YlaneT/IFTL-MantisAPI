package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.FieldNotFoundException;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.model.Issue;

import java.util.List;

public interface IssuesService {
    // Get an issue
    Issue searchIssue (int id) throws IssueNotFoundException;
    
    Issue searchIssue (int id, List<String> selectValues) throws IssueNotFoundException, FieldNotFoundException;
    
    // Get all issues
    List<Issue> searchAllIssues(int pageSize, int page, List<String> selectValues);
}
