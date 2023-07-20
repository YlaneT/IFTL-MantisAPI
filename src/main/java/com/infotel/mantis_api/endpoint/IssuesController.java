package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.IssueRecap;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.IssuesServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    @GetMapping("/{id}")
    public Issue getIssue(@PathVariable("id") int id) {
        IssuesService service = new IssuesServiceImpl();
        return service.searchIssue(id);
    }

    @GetMapping()
    public List<IssueRecap> getAllIssues(@RequestParam("pageSize") int pageSize, @RequestParam("page") int page) {
        IssuesService service = new IssuesServiceImpl();
        return service.searchAllIssues(pageSize, page);
    }

    @GetMapping()
    public List<IssueRecap> getAllIssues() {
        IssuesService service = new IssuesServiceImpl();
        return service.searchAllIssues(50, 1);
    }
}
