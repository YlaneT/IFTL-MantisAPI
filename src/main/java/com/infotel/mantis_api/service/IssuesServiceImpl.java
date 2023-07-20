package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.IssueRecap;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class IssuesServiceImpl implements IssuesService {
    
    private static LocalDateTime parseDate (String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    @Override
    public Issue searchIssue (int id) {
        WebDriver driver = Authenticator.login();
        Issue     issue  = new Issue();
        
        // FIXME? use url : http://localhost/mantisbt/view.php?id=1
        driver.findElement(By.name("bug_id")).sendKeys(String.valueOf(id));
        driver.findElement(By.xpath("//input[@value='Jump']")).click();
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        // TODO: Gestion de l'erreur si on ne trouve pas l'issue
        
        // Mandatory fields
        WebElement idElement = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[1]"));
        issue.setId(idElement.getText());
        
        WebElement projectElement = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[2]"));
        issue.setProject(projectElement.getText());
        
        WebElement categoryElement = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[3]"));
        issue.setCategory(categoryElement.getText());
        
        WebElement submittedElement = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[5]"));
        issue.setSubmitted(parseDate(submittedElement.getText()));
        
        WebElement lastUpdatedElement = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[6]"));
        issue.setLastUpdated(parseDate(lastUpdatedElement.getText()));
        
        WebElement reporterElement = driver.findElement(By.xpath("//table[3]/tbody/tr[5]/td[2]"));
        issue.setReporter(reporterElement.getText());
        
        WebElement assignedElement = driver.findElement(By.xpath("//table[3]/tbody/tr[6]/td[2]"));
        issue.setAssigned(assignedElement.getText());
        
        WebElement priorityElement = driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[2]"));
        issue.setPriority(priorityElement.getText());
        
        WebElement statusElement = driver.findElement(By.xpath("//table[3]/tbody/tr[8]/td[2]"));
        issue.setStatus(statusElement.getText());
        
        WebElement severityElement = driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[4]"));
        issue.setSeverity(severityElement.getText());
        
        WebElement reproducibilityElement = driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[6]"));
        issue.setReproducibility(reproducibilityElement.getText());
        
        WebElement summaryElement = driver.findElement(By.xpath("//table[3]/tbody/tr[11]/td[2]"));
        issue.setSummary(summaryElement.getText());
        
        WebElement descriptionElement = driver.findElement(By.xpath("//table[3]/tbody/tr[12]/td[2]"));
        issue.setDescription(descriptionElement.getText());
        
        // Tags
        WebElement tagsCategoryElement = driver.findElement(By.xpath("//td[text()='Tags' and @class='category']"));
        
        if (!tagsCategoryElement.getText().equals("No tags attached.")) {
            // Find immediate sibling of Tags header
            WebElement tagsValueElement = tagsCategoryElement.findElement(By.xpath("./following-sibling::td"));
            // Extract links containing text, not delete cross
            List<WebElement> links = tagsValueElement.findElements(By.cssSelector("a"));
            for(int i = 0 ; i < links.size() ; i += 2) {
                issue.getTags().add(links.get(i).getText());
            }
        }
        
        /* Optional fields */
        // Steps to reproduce
        try {
            WebElement strTitle         = driver.findElement(By.xpath(
                "//td[text()='Steps To Reproduce' and @class='category']"));
            WebElement strElement       = strTitle.findElement(By.xpath("./following-sibling::td"));
            String     stepsToReproduce = strElement.getText();
            issue.setStepsToReproduce(stepsToReproduce);
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
        }
        
        // Additional Information
        try {
            WebElement aiTitle          = driver.findElement(By.xpath(
                "//td[text()='Additional Information' and @class='category']"));
            WebElement strElement       = aiTitle.findElement(By.xpath("./following-sibling::td"));
            String     stepsToReproduce = strElement.getText();
            issue.setAdditionalInformation(stepsToReproduce);
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
        }
        
        driver.quit();
        return issue;
    }
    
    @Override
    public List<Issue> searchAllIssues (int pageSize, int page, List<String> select, int projectId, String filterId) {
        return null;
    }
    
    public List<IssueRecap> searchAllIssues () {
        return searchAllIssues(10, 1);
    }
    
    @Override
    public List<IssueRecap> searchAllIssues (int pageSize, int page) {
        WebDriver driver = Authenticator.login();
        
        driver.get("http://localhost/mantisbt/view_all_bug_page.php");
        
        WebElement       buglist   = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<IssueRecap> issues    = new ArrayList<>();
        
        for(int i = 3 ; i < issueRows.size() - 1 ; i++) {
            WebElement issueRow = issueRows.get(i);
            IssueRecap issue    = new IssueRecap();
            
            List<WebElement> columns    = issueRow.findElements(By.tagName("td"));
            List<String>     strColumns = new ArrayList<>();
            
            for(WebElement col : columns) {
                strColumns.add(col.getText());
            }
            
            issue.setId(strColumns.get(3));
            //            issue.setPriority(strColumns.get(4)); // FIXME: get Title (not text)
            //            issue.setAttachmentCount(strColumns.get(5)); // FIXME: Get number (not link)
            issue.setCategory(strColumns.get(6));
            issue.setSeverity(strColumns.get(7));
            issue.setStatus(strColumns.get(8));
            LocalDate date = LocalDate.parse(strColumns.get(9), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            issue.setUpdated(date);
            issue.setSummary(strColumns.get(10));
            
            
            issues.add(issue);
        }
        return issues;
    }
}
