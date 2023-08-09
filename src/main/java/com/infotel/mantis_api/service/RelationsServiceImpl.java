package com.infotel.mantis_api.service;

import com.infotel.mantis_api.endpoint.IssueFilesController;
import com.infotel.mantis_api.exception.AccessDenied;
import com.infotel.mantis_api.model.Relation;
import com.infotel.mantis_api.model.Relations;
import com.infotel.mantis_api.util.Authenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationsServiceImpl implements RelationsService {
    
    private final Logger log = LogManager.getLogger(IssueFilesController.class);
    @Autowired
    Authenticator auth;
    @Value("${mantis.base-url}")
    private String baseUrl;
    
    @Override
    public List<Relation> searchRelations (int id) {
        return null;
    }
    
    @Override
    public String addRelations (int id, Relations relations) throws AccessDenied {
        WebDriver driver = auth.login();
        driver.get(baseUrl + "/view.php?id=" + id);
        
        List<Relation> oldRelations = searchRelations(id);
        
        WebElement encompassingTable   = driver.findElement(By.id("relationships_open"));
        // TODO: Récupérer les relations avec searchRelations
        WebElement newRelationshipCell = encompassingTable.findElement(By.xpath("//tr[2]/td[2]"));
        
        for(Integer issue_id : relations.getParentOf()) {
            // TODO : Checker si UNE relation existe déjà
            // (if issue_id est dans la liste oldRelations)
            
            //      Si oui, checker si c'est le même type de relations avec
            //      if (matching_relation.getType().equals("parent of")
            
            //          Si oui, ne rien faire
            //          Si non, ajouter et valider le message de confirmation puis réinitialiser encompassingTable et
            //           newRelationshipCell (pour éviter StaleElementException)
            
            new Select(driver.findElement(By.name("rel_type"))).selectByValue("2");
//            new Select(driver.findElement(By.name("rel_type"))).selectByVisibleText("parent of");
            driver.findElement(By.name("dest_bug_id")).sendKeys(String.valueOf(id));
            driver.findElement(By.name("add_relationship")).click();
        }
        // FIXME: Implement
        return null;
    }
    
    @Override
    public String deleteRelations (int id, Relations relations) {
        return null;
    }
}
