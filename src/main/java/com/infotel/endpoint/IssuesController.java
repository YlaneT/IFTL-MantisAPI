package com.infotel.endpoint;

import com.infotel.model.Issue;
import com.infotel.service.IssuesService;
import com.infotel.service.IssuesServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    @GetMapping("/{id}")
    public Issue getIssue(@PathVariable("id") int id) {
        IssuesService service = new IssuesServiceImpl();
        return service.searchIssue(id);
    }
}
