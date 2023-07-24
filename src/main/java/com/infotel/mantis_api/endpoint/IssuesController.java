package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.IssuesServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    public List<Issue> getAllIssues(@RequestParam(value = "pageSize", defaultValue = "50") int pageSize,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value="select", required = false) String select) {
        List<String> selectValues = new ArrayList<>();
        if (select != null){
            selectValues = Arrays.asList(select.split(","));
        }
        IssuesService service = new IssuesServiceImpl();
        return service.searchAllIssues(pageSize, page, selectValues);
    }
}
