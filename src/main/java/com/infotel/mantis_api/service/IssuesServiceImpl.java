package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.model.IssueRecap;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        
        extractAllMandatoryFields(issue, driver);
        extractAllOptionalFields(issue, driver);
        
        driver.quit();
        return issue;
    }
    
    @Override
    public Issue searchIssue (int id, List<String> selectValues) {
        WebDriver driver = Authenticator.login();
        Issue     issue  = new Issue();
        
        // FIXME? use url : http://localhost/mantisbt/view.php?id=1
        driver.findElement(By.name("bug_id")).sendKeys(String.valueOf(id));
        driver.findElement(By.xpath("//input[@value='Jump']")).click();
        
        for(String selected : selectValues) {
            if (issueTab.containsKey(selected.toLowerCase())) {
                issueTab.get(selected.toLowerCase());
            } else {
                try {
                    WebElement customFieldElem =
                        driver.findElement(By.xpath("//td[text()='"+selected+"' and @class='category']"));
                    WebElement   customFieldValElem =
                        customFieldElem.findElement(By.xpath("./following-sibling::td"));
                    
                    issue.getCustomFields().put(customFieldElem.getText(), customFieldValElem.getText());
                } catch (NoSuchElementException e) {
                    // TODO: Throw exception Issue field not found
                    System.err.println("\"" + selected + "\" not found.");
                }
            }
        }
        return issue;
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
    
    private void extractAllMandatoryFields (Issue issue, WebDriver driver) {
        issue.setId(extractId(driver));
        issue.setProject(extractProject(driver));
        issue.setCategory(extractCategory(driver));
        issue.setViewStatus(extractViewStatus(driver));
        issue.setSubmitted(extractSubmitted(driver));
        issue.setLastUpdated(extractUpdated(driver));
        issue.setReporter(extractReporter(driver));
        issue.setAssigned(extractAssigned(driver));
        issue.setPriority(extractPriority(driver));
        issue.setSeverity(extractSeverity(driver));
        issue.setReproducibility(extractReproducibility(driver));
        issue.setStatus(extractStatus(driver));
        issue.setResolution(extractResolution(driver));
        issue.setPlatform(extractPlatform(driver));
        issue.setOs(extractOs(driver));
        issue.setOsVersion(extractOsVersion(driver));
        issue.setSummary(extractSummary(driver));
        issue.setDescription(extractDescription(driver));
        issue.setTags(extractTags(driver));
    }
    
    private void extractAllOptionalFields (Issue issue, WebDriver driver) {
        issue.setStepsToReproduce(extractStepsToReproduce(driver));
        issue.setAdditionalInformation(extractAdditionalInformation(driver));
    }
    
    private String extractId (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[1]")).getText();
    }
    
    private String extractProject (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[2]")).getText();
    }
    
    private String extractCategory (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[3]")).getText();
    }
    
    private String extractViewStatus (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[4]")).getText();
    }
    
    private LocalDateTime extractSubmitted (WebDriver driver) {
        WebElement element      = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[5]"));
        String     submittedStr = element.getText();
        return LocalDateTime.parse(submittedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    private LocalDateTime extractUpdated (WebDriver driver) {
        WebElement element    = driver.findElement(By.xpath("//table[3]/tbody/tr[3]/td[6]"));
        String     updatedStr = element.getText();
        return LocalDateTime.parse(updatedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    private String extractReporter (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[5]/td[2]")).getText();
    }
    
    private String extractAssigned (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[6]/td[2]")).getText();
    }
    
    private String extractPriority (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[2]")).getText();
    }
    
    private String extractSeverity (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[4]")).getText();
    }
    
    private String extractReproducibility (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[7]/td[6]")).getText();
    }
    
    private String extractStatus (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[8]/td[2]")).getText();
    }
    
    private String extractResolution (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[8]/td[4]")).getText();
    }
    
    private String extractPlatform (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[9]/td[2]")).getText();
    }
    
    private String extractOs (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[9]/td[4]")).getText();
    }
    
    private String extractOsVersion (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[9]/td[6]")).getText();
    }
    
    private String extractSummary (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[11]/td[2]")).getText();
    }
    
    private String extractDescription (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[12]/td[2]")).getText();
    }
    
    private List<String> extractTags (WebDriver driver) {
        WebElement tagsCategoryElement = driver.findElement(By.xpath("//td[text()='Tags' and @class='category']"));
        // Find immediate sibling of Tags header
        WebElement   tagsValueElement = tagsCategoryElement.findElement(By.xpath("./following-sibling::td"));
        List<String> tags             = new ArrayList<>();
        if (!tagsValueElement.getText().equals("No tags attached.")) {
            // Extract links containing text, not delete cross
            List<WebElement> links = tagsValueElement.findElements(By.cssSelector("a"));
            for(int i = 0 ; i < links.size() ; i += 2) {
                tags.add(links.get(i).getText());
            }
        }
        return tags;
    }
    
    
    private String extractStepsToReproduce (WebDriver driver) {
        try {
            WebElement strTitle = driver.findElement(By.xpath("//td[text()='Steps To Reproduce' and " +
                "@class='category']"));
            WebElement strElement       = strTitle.findElement(By.xpath("./following-sibling::td"));
            String     stepsToReproduce = strElement.getText();
            return stepsToReproduce;
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
            return null;
        }
    }
    
    private String extractAdditionalInformation (WebDriver driver) {
        try {
            WebElement aiTitle = driver.findElement(By.xpath("//td[text()='Additional Information' and " +
                "@class='category']"));
            WebElement strElement       = aiTitle.findElement(By.xpath("./following-sibling::td"));
            String     additionalInformation = strElement.getText();
            return additionalInformation;
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
            return null;
        }
    }
}
