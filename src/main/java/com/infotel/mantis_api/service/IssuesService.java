package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;

import java.util.List;

public interface IssuesService {
    Issue searchIssue (int id);
    List<Issue> searchAllIssues (int pageSize, int page, List<String> select, int projectId, String filterId);

    List<Issue> searchAllIssues(int pageSize, int page, List<String> selectValues);
}
