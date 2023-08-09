package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.Relation;
import com.infotel.mantis_api.model.Relations;
import com.infotel.mantis_api.service.IssuesService;
import com.infotel.mantis_api.service.RelationsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/relation")
public class RelationshipsController {
    
    private final Logger log = LogManager.getLogger(RelationshipsController.class);
    @Autowired
    RelationsService service;
    
    @GetMapping("/{id}")
    public List<Relation> getIssueRelations (@PathVariable("id") int id){
        log.info("ENDPOINT Get issue relations with parameter issue_id=\"%d\"".formatted(id));
        return service.searchRelations(id);
    }
    
    @PostMapping("/{id}")
    public String addIssueRelations (@PathVariable("id") int id, @RequestBody Relations relations){
        log.info("ENDPOINT Get issue relations with parameter issue_id=\"%d\"".formatted(id));
        return service.addRelations(id, relations);
    }
    
    @DeleteMapping("/{id}")
    public String removeIssueRelations (@PathVariable("id") int id, @RequestBody Relations relations){
        log.info("ENDPOINT Delete issue relations with parameter issue_id=\"%d\"".formatted(id));
        return service.deleteRelations(id, relations);
    }
    
}
