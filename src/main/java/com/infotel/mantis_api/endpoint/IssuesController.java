package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.*;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.service.IssuesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    private final Logger log = LogManager.getLogger(IssueFilesController.class);
    @Autowired
    IssuesService service;
    
    @GetMapping("/{id}")
    public Issue getIssue (
        @PathVariable("id") int id, @RequestParam(value = "select", required = false) String select
    ) {
        log.info("ENDPOINT Get issue with parameters issue_id=\"%d\", select=\"%s\"".formatted(id, select));
        
        try {
            if (select == null) {
                return service.searchIssue(id);
            }
            else {
                List<String> selectValues = Arrays.asList(select.split(","));
                return service.searchIssue(id, selectValues);
            }
        } catch (IssueNotFoundException e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (FieldNotFoundException e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @GetMapping()
    public List<Issue> getAllIssues (
        @RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "select", required = false) String select,
        @RequestParam(value = "project_id", defaultValue = "0") int projectId
    ) {
        log.info("ENDPOINT Get all issue with parameters pageSize=\"%d\", page=\"%d\", select=\"%s\"".formatted(
            pageSize, page, select
        ));
        List<String> selectValues = select == null ? new ArrayList<>() : Arrays.asList(select.split(","));
        try {
            return service.searchAllIssues(pageSize, page, selectValues, projectId);
        } catch (FieldNotFoundException | ProjectNotFoundException e) {
            log.warn(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
