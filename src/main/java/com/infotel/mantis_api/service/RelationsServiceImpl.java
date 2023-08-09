package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Relation;
import com.infotel.mantis_api.model.Relations;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.infotel.mantis_api.util.extract_from.IssueDetails.extractProject;

@Service
public class RelationsServiceImpl implements RelationsService {
    @Autowired
    Authenticator auth;
    @Value("${mantis.base-url}")
    private String baseUrl;

    private static LocalDateTime parseDate (String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }


    @Override
    public List<Relation> searchRelations (int id) {

        WebDriver driver = auth.login();
        driver.get(baseUrl + "/view.php?id=" + id);

        List<Relation> relations = new ArrayList<>();

        WebElement relationTable = driver.findElement(By.id("relationships_open"));
        WebElement encompassingTable = relationTable.findElement(By.tagName("table"));
        WebElement relationshipsTable;
        try {
            relationshipsTable = encompassingTable.findElement(By.tagName("table"));
        } catch (NoSuchElementException e) {
            driver.quit();
            return relations;
        }

        List<WebElement> table_1 = relationshipsTable.findElements(By.tagName("tr"));


        int nbCol = table_1.get(0).findElements(By.tagName("td")).size();
        String projectName = nbCol==5 ? extractProject(driver) : "";

        for (WebElement row: table_1) {
            List<WebElement> tdList = row.findElements(By.tagName("td"));
            Relation r = new Relation();

            if (tdList.size() != 1) {
                r.setType(tdList.get(0).getText().trim());
                r.setIssue_id(Integer.parseInt(tdList.get(1).getText()));
                r.setStatus(tdList.get(2).getText());
                if (nbCol == 5) {
                    r.setProject(projectName);
                    r.setAssigned(tdList.get(3).getText());
                    r.setSummary(tdList.get(4).getText());
                } else if (nbCol == 6) {
                    r.setProject(tdList.get(3).getText());
                    r.setAssigned(tdList.get(4).getText());
                    r.setSummary(tdList.get(5).getText().replace(" [Delete] ", "")); // TODO : Enlever [Delete]
                }
                relations.add(r);
            }
        }
        driver.quit();
        return relations;
    }
    
    @Override
    public String addRelations (int id, Relations relations) {
        return null;
    }
    
    @Override
    public String deleteRelations (int id, Relations relations) {
        return null;
    }
}

