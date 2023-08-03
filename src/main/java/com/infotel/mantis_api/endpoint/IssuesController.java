package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.FieldNotFoundException;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.Note;
import com.infotel.mantis_api.service.IssuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    @Autowired
    IssuesService service;
    
    @GetMapping("/{id}")
    public Issue getIssue (@PathVariable("id") int id,
        @RequestParam(value = "select", required = false) String select) {
        
        
        try {
            if (select == null) {
                return service.searchIssue(id);
            }
            else {
                List<String> selectValues = Arrays.asList(select.split(","));
                return service.searchIssue(id, selectValues);
            }
        } catch (IssueNotFoundException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (FieldNotFoundException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @GetMapping()
    public List<Issue> getAllIssues (@RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "select", required = false) String select) {
        
        try {
            if (select == null) {
                return service.searchAllIssues(pageSize, page);
            }
            else {
                List<String> selectValues = Arrays.asList(select.split(","));
                return service.searchAllIssues(pageSize, page, selectValues);
            }
        } catch (FieldNotFoundException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public void createIssue(@RequestBody Issue issue) {
        String category = issue.getCategory();
        String reproducibility = issue.getReproducibility();
        String severity = issue.getSeverity();
        String priority = issue.getPriority();
        String platform = issue.getPlatform();
        String os = issue.getOs();
        String osVersion = issue.getOsVersion();
        String assigned = issue.getAssigned();
        String summary = issue.getSummary();
        String description = issue.getDescription();
        String stepsToReproduce = issue.getStepsToReproduce();
        String additionalInformation = issue.getAdditionalInformation();

        IssuesService service = new IssuesServiceImpl();

        // TODO: try catch
        try {
            service.createIssue(category, reproducibility, severity, priority, platform,
                    os, osVersion, assigned, summary, description, stepsToReproduce, additionalInformation);
        } catch (FieldNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Dans le catch, throw ResponseStatusException avec BAD_REQUEST pcq l'utilisateur a mal rempli
    }

    @PostMapping("/{id}/note")
    public void addNote(@PathVariable("id") int id,@RequestBody Note note) {

        String noteField = note.getNote();

        IssuesService service = new IssuesServiceImpl();

        service.addNote(id, noteField);

    }
}
