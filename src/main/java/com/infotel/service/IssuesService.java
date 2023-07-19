package com.infotel.service;

import com.infotel.model.Issue;

import java.util.List;

public interface IssuesService {
    Issue searchIssue (int id);
    List<Issue> searchAllIssues (int pageSize, int page, List<String> select, int projectId, String filterId);
}
