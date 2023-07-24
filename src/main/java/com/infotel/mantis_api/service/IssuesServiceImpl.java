package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.CustomFieldNotFoundException;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IssuesServiceImpl implements IssuesService {
    
    @Override
    public Issue searchIssue (int id) throws IssueNotFoundException {
        WebDriver driver = Authenticator.login();
        Issue     issue  = new Issue();
        
        // FIXME? use url : http://localhost/mantisbt/view.php?id=1
        driver.findElement(By.name("bug_id")).sendKeys(String.valueOf(id));
        driver.findElement(By.xpath("//input[@value='Jump']")).click();
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        
        try {
            extractAllMandatoryFields(issue, driver);
            extractAllOptionalFields(issue, driver);
        } catch (NoSuchElementException e) {
            driver.quit();
            String message = driver.findElement(By.xpath("//p[contains(text(),'not found')]")).getText();
            throw new IssueNotFoundException(message);
        }
        driver.quit();
        return issue;
    }
    
    @Override
    public Issue searchIssue (int id, List<String> selectValues) throws IssueNotFoundException, CustomFieldNotFoundException {
        WebDriver driver = Authenticator.login();
        Issue     issue  = new Issue();
        
        // FIXME? use url : http://localhost/mantisbt/view.php?id=1
        driver.findElement(By.name("bug_id")).sendKeys(String.valueOf(id));
        driver.findElement(By.xpath("//input[@value='Jump']")).click();
        Map<String, Runnable> issueTab = new HashMap<>();
        issueTab.put("id", () -> issue.setId(extractId(driver)));
        issueTab.put("project", () -> issue.setProject(extractProject(driver)));
        issueTab.put("category", () -> issue.setCategory(extractCategory(driver)));
        issueTab.put("view status", () -> issue.setViewStatus(extractViewStatus(driver)));
        issueTab.put("submitted", () -> issue.setSubmitted(extractSubmitted(driver)));
        issueTab.put("updated", () -> issue.setLastUpdated(extractUpdated(driver)));
        issueTab.put("reporter", () -> issue.setReporter(extractReporter(driver)));
        issueTab.put("assigned", () -> issue.setAssigned(extractAssigned(driver)));
        issueTab.put("priority", () -> issue.setPriority(extractPriority(driver)));
        issueTab.put("severity", () -> issue.setSeverity(extractSeverity(driver)));
        issueTab.put("reproducibility", () -> issue.setReproducibility(extractReproducibility(driver)));
        issueTab.put("status", () -> issue.setStatus(extractStatus(driver)));
        issueTab.put("resolution", () -> issue.setResolution(extractResolution(driver)));
        issueTab.put("platform", () -> issue.setPlatform(extractPlatform(driver)));
        issueTab.put("os", () -> issue.setOs(extractOs(driver)));
        issueTab.put("os version", () -> issue.setOsVersion(extractOsVersion(driver)));
        issueTab.put("summary", () -> issue.setSummary(extractSummary(driver)));
        issueTab.put("description", () -> issue.setDescription(extractDescription(driver)));
        issueTab.put("tags", () -> issue.setTags(extractTags(driver)));
        issueTab.put("steps", () -> issue.setStepsToReproduce(extractStepsToReproduce(driver)));
        issueTab.put("additional info", () -> issue.setAdditionalInformation(extractAdditionalInformation(driver)));
        
        for(String selected : selectValues) {
            if (issueTab.containsKey(selected.toLowerCase())) {
                try {
                    Runnable runnable = issueTab.get(selected.toLowerCase());
                    runnable.run();
                } catch (NoSuchElementException e) {
                    String message = driver.findElement(By.xpath("//p[contains(text(),'not found')]")).getText();
                    driver.quit();
                    throw new IssueNotFoundException(message);
                }
            }
            else {
                try {
                    WebElement customFieldElem =
                        driver.findElement(By.xpath("//td[text()='" + selected + "' and @class='category']"));
                    WebElement customFieldValElem = customFieldElem.findElement(By.xpath("./following-sibling::td"));
                    
                    issue.getCustomFields().put(customFieldElem.getText(), customFieldValElem.getText());
                } catch (NoSuchElementException e) {
                    driver.quit();
                    throw new CustomFieldNotFoundException("\"" + selected + "\" field not found.");
                }
            }
        }
        driver.quit();
        return issue;
    }
    
    private List<Issue> searchAllIssues () {
        return searchAllIssues(10, 1);
    }
    
    /**
     * @param pageSize number of issues per page
     * @param page     number of the page shown
     * @return recap of all issues on the page
     */
    private List<Issue> searchAllIssues (int pageSize, int page) {
        WebDriver driver = Authenticator.login();
        
        driver.get("http://localhost/mantisbt/view_all_bug_page.php");
        
        WebElement buglist = driver.findElement(By.id("buglist"));
        
        // TODO: Gérer la pagination
        
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<Issue>      issues    = new ArrayList<>();
        
        for(int i = 3 ; i < issueRows.size() - 1 ; i++) {
            WebElement issueRow = issueRows.get(i);
            Issue      issue    = new Issue();
            
            List<WebElement> columns    = issueRow.findElements(By.tagName("td"));
            List<String>     strColumns = new ArrayList<>();
            
            for(WebElement col : columns) {
                strColumns.add(col.getText());
            }
            
            //            issue.setPriority(strColumns.get(2)); // FIXME: get Title (not text)
            issue.setId(strColumns.get(3));
            //            issue.setAttachmentCount(strColumns.get(5)); // FIXME: Get number (not link)
            issue.setCategory(strColumns.get(6));
            issue.setSeverity(strColumns.get(7));
            issue.setStatus(strColumns.get(8));
            
            issue.setSummary(strColumns.get(10));
            
            
            String date = buglist.findElement(By.xpath("//tr[" + (i + 1) + "]/td[" + 10 + "]")).getText();
            issue.setLastUpdated(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());
            
            issues.add(issue);
        }
        driver.quit();
        return issues;
    }
    
    /**
     * @param pageSize     number of issues per page
     * @param page         number of the page shown
     * @param selectValues fields to be shown
     * @return selected fields of all issues on the page
     */
    public List<Issue> searchAllIssues (int pageSize, int page, List<String> selectValues) {
        if (selectValues.isEmpty()) {
            return searchAllIssues(pageSize, page);
        }
        
        // FIXME
        WebDriver driver = Authenticator.login();
        
        driver.get("http://localhost/mantisbt/view_all_bug_page.php");
        
        WebElement       buglist   = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<Issue>      issues    = new ArrayList<>();
        
        for(WebElement row : issueRows) {
            Issue issue = new Issue();
            
            if (selectValues.contains("id")) {
                WebElement issueId = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/td[4]"));
                issueId.getText();
                // issue.setId();
            }
            else if (selectValues.contains("summary")) {
                WebElement summaryIssue = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/tr[11]"));
                summaryIssue.getText();
                //  issue.setSummary();
            }
            else if (selectValues.contains("description")) {
                WebElement issueDetail = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/td[4]"));
                issueDetail.click();
                extractDescription(driver);
                // issue.setDescription();
            }
            else {
                driver.get("http://localhost/mantisbt/view_all_bug_page.php");
            }
            issues.add(issue);
        }


    /*
        WebElement issueID = driver.findElement(By.cssSelector("a[href='/mantisbt/view.php?id=3']"));
        issueID.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        driver.get("http://localhost/mantisbt/view.php?id=3");
        */
        
        driver.quit();
        return null;
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
    
    private String extractFromIssueTab (WebDriver driver, int x, int y) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[" + x + "]/td[" + y + "]")).getText();
    }
    
    private String extractId (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 1);
    }
    
    private String extractProject (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 2);
    }
    
    private String extractCategory (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 3);
    }
    
    private String extractViewStatus (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 4);
    }
    
    private LocalDateTime extractSubmitted (WebDriver driver) {
        String submittedStr = extractFromIssueTab(driver, 3, 5);
        return parseDate(submittedStr);
    }
    
    private LocalDateTime extractUpdated (WebDriver driver) {
        String updatedStr = extractFromIssueTab(driver, 3, 6);
        return parseDate(updatedStr);
    }
    
    private String extractReporter (WebDriver driver) {
        return extractFromIssueTab(driver, 5, 2);
    }
    
    private String extractAssigned (WebDriver driver) {
        return extractFromIssueTab(driver, 6, 2);
    }
    
    private String extractPriority (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 2);
    }
    
    private String extractSeverity (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 4);
    }
    
    private String extractReproducibility (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 6);
    }
    
    private String extractStatus (WebDriver driver) {
        return extractFromIssueTab(driver, 8, 2);
    }
    
    private String extractResolution (WebDriver driver) {
        return extractFromIssueTab(driver, 8, 4);
    }
    
    private String extractPlatform (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 2);
    }
    
    private String extractOs (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 4);
    }
    
    private String extractOsVersion (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 6);
    }
    
    private String extractSummary (WebDriver driver) {
        return extractFromIssueTab(driver, 11, 2);
    }
    
    private String extractDescription (WebDriver driver) {
        return extractFromIssueTab(driver, 12, 2);
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
            WebElement strTitle =
                driver.findElement(By.xpath("//td[text()='Steps To Reproduce' and @class='category']"));
            WebElement strElement       = strTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
            return null;
        }
    }
    
    private String extractAdditionalInformation (WebDriver driver) {
        try {
            WebElement aiTitle =
                driver.findElement(By.xpath("//td[text()='Additional Information' and @class='category']"));
            WebElement strElement = aiTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
            return null;
        }
    }
    
    private static LocalDateTime parseDate (String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
