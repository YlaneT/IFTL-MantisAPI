package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.IssueRecap;

import java.util.List;

public interface IssuesService {
    // Get an issue
    Issue searchIssue (int id);
    
    Issue searchIssue (int id, List<String> selectValues);
    
    // Get all issues
    List<IssueRecap> searchAllIssues ();
    
    List<IssueRecap> searchAllIssues (int pageSize, int page);
}
