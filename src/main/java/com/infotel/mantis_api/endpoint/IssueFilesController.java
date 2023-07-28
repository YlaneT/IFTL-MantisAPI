package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.service.IssueFilesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/issues/{issue_id}/files")
public class IssueFilesController {
    
    private final Logger log = LogManager.getLogger(IssueFilesController.class);
    @Autowired
    IssueFilesService service;
    
    @GetMapping("/{file_id}")
    public String getIssueFile (@PathVariable("issue_id") int issueId, @PathVariable("file_id") int fileId) {
        log.info("ENDPOINT Get issue file with parameters issue_id=\"%d\", file_id=\"%d\"".formatted(issueId, fileId));
        try {
            return service.searchIssueFile(issueId, fileId);
        } catch (IssueNotFoundException | IssueFileNotFound e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @GetMapping()
    public List<String> getAllIssueFiles (@PathVariable("issue_id") int issueId) {
        log.info("ENDPOINT Get all issue files with parameter issue_id=\"%d\"".formatted(issueId));
        try {
            return service.searchAllIssueFiles(issueId);
        } catch (IssueNotFoundException e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @DeleteMapping("/{file_id}/delete")
    public String deleteIssueFile (@PathVariable("issue_id") int issueId, @PathVariable("file_id") int fileId) {
        log.info(
            "ENDPOINT Delete issue file with parameters issue_id=\"%d\", file_id=\"%d\"".formatted(issueId, fileId));
        try {
            service.deleteIssueFile(issueId, fileId);
            return "Deleted file " + fileId + " from issue " + issueId;
        } catch (IssueNotFoundException | IssueFileNotFound e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
