package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.IssueRecap;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.IssuesServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/issues")
public class IssuesController {
    
    @GetMapping("/{id}")
    public Issue getIssue (@PathVariable("id") int id,
        @RequestParam(value = "select", required = false) String select) {
        IssuesService service = new IssuesServiceImpl();
        
        if (select == null){
            return service.searchIssue(id);
        } else {
            List<String> selectValues = Arrays.asList(select.split(","));
            return service.searchIssue(id, selectValues);
        }
    }
    
    @GetMapping()
    public List<IssueRecap> getAllIssues(@RequestParam("pageSize") int pageSize, @RequestParam("page") int page) {
        IssuesService service = new IssuesServiceImpl();
        return service.searchAllIssues(pageSize, page);
    }
}
