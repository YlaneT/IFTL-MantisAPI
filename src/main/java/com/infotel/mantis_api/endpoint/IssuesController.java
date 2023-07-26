package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.FieldNotFoundException;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.IssuesServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    @GetMapping("/{id}")
    public Issue getIssue (@PathVariable("id") int id,
        @RequestParam(value = "select", required = false) String select) {
        IssuesService service = new IssuesServiceImpl();
        
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
    public List<Issue> getAllIssues(@RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value="select", required = false) String select) {
        IssuesService service = new IssuesServiceImpl();
        
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
}
