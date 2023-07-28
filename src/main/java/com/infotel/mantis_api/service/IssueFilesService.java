package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.exception.IssueNotFoundException;

import java.util.List;

public interface IssueFilesService {
    // Get a file from an issue
    String searchIssueFile (int id, int fileId) throws IssueNotFoundException, IssueFileNotFound;
    
    // Get all files from an issue
    List<String> searchAllIssueFiles (int issueId) throws IssueNotFoundException;
    
    // Remove a file from an issue
    void deleteIssueFile (int id, int fileId) throws IssueNotFoundException, IssueFileNotFound;
}
