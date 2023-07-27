package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.service.IssueFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/issues/{issue_id}/files")
public class IssueFilesController {
    
    @Autowired
    IssueFilesService service;
    
    @GetMapping("/{file_id}")
    public String getIssueFile (@PathVariable("issue_id") int issueId, @PathVariable("file_id") int fileId) {
        try {
            return service.searchIssueFile(issueId, fileId);
        } catch (IssueNotFoundException | IssueFileNotFound e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @GetMapping()
    public List<String> getAllIssueFiles (@PathVariable("issue_id") int issueId) {
        try {
            return service.searchAllIssueFiles(issueId);
        } catch (IssueNotFoundException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
