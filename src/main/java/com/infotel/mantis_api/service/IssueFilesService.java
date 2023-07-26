package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.IssueNotFoundException;

import java.util.List;

public interface IssueFilesService {
    // Get a file from an issue
    String searchIssueFile (int id, int fileId);
    
    // Get all files from an issue
    List<String> searchAllIssueFiles (int issueId) throws IssueNotFoundException;
}
