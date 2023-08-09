package com.infotel.mantis_api.endpoint;

import com.infotel.mantis_api.exception.AccessDenied;
import com.infotel.mantis_api.model.Relation;
import com.infotel.mantis_api.model.Relations;
import com.infotel.mantis_api.service.RelationsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        log.info("ENDPOINT Add issue relations with parameter issue_id=\"%d\"".formatted(id));
        try {
            return service.addRelations(id, relations);
        } catch (AccessDenied e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public String removeIssueRelations (@PathVariable("id") int id, @RequestBody Relations relations){
        log.info("ENDPOINT Delete issue relations with parameter issue_id=\"%d\"".formatted(id));
        return service.deleteRelations(id, relations);
    }
    
}
