package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.IssuesServiceImpl;
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
