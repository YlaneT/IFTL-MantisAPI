package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.FieldNotFoundException;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.util.Authenticator;
import com.infotel.mantis_api.util.extract_from.IssueDetails;
import com.infotel.mantis_api.util.extract_from.IssueRecap;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

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
            IssueDetails.extractAndSetAllMandatoryFields(issue, driver);
            IssueDetails.extractAndSetAllOptionalFields(issue, driver);
        } catch (NoSuchElementException e) {
            String message = driver.findElement(By.xpath("//p[contains(text(),'not found')]")).getText();
            driver.quit();
            throw new IssueNotFoundException(message);
        }
        driver.quit();
        return issue;
    }
    
    @Override
    public Issue searchIssue (int id, List<String> selectValues) throws IssueNotFoundException, FieldNotFoundException {
        WebDriver driver = Authenticator.login();
        Issue     issue  = new Issue();
        
        // FIXME? use url : http://localhost/mantisbt/view.php?id=1
        driver.findElement(By.name("bug_id")).sendKeys(String.valueOf(id));
        driver.findElement(By.xpath("//input[@value='Jump']")).click();
        Map<String, Runnable> issueTab = new HashMap<>();
        issueTab.put("id", () -> issue.setId(IssueDetails.extractId(driver)));
        issueTab.put("project", () -> issue.setProject(IssueDetails.extractProject(driver)));
        issueTab.put("category", () -> issue.setCategory(IssueDetails.extractCategory(driver)));
        issueTab.put("view status", () -> issue.setViewStatus(IssueDetails.extractViewStatus(driver)));
        issueTab.put("submitted", () -> issue.setSubmitted(IssueDetails.extractSubmitted(driver)));
        issueTab.put("updated", () -> issue.setLastUpdated(IssueDetails.extractUpdated(driver)));
        issueTab.put("reporter", () -> issue.setReporter(IssueDetails.extractReporter(driver)));
        issueTab.put("assigned", () -> issue.setAssigned(IssueDetails.extractAssigned(driver)));
        issueTab.put("priority", () -> issue.setPriority(IssueDetails.extractPriority(driver)));
        issueTab.put("severity", () -> issue.setSeverity(IssueDetails.extractSeverity(driver)));
        issueTab.put("reproducibility", () -> issue.setReproducibility(IssueDetails.extractReproducibility(driver)));
        issueTab.put("status", () -> issue.setStatus(IssueDetails.extractStatus(driver)));
        issueTab.put("resolution", () -> issue.setResolution(IssueDetails.extractResolution(driver)));
        issueTab.put("platform", () -> issue.setPlatform(IssueDetails.extractPlatform(driver)));
        issueTab.put("os", () -> issue.setOs(IssueDetails.extractOs(driver)));
        issueTab.put("os version", () -> issue.setOsVersion(IssueDetails.extractOsVersion(driver)));
        issueTab.put("summary", () -> issue.setSummary(IssueDetails.extractSummary(driver)));
        issueTab.put("description", () -> issue.setDescription(IssueDetails.extractDescription(driver)));
        issueTab.put("tags", () -> issue.setTags(IssueDetails.extractTags(driver)));
        issueTab.put("steps", () -> issue.setStepsToReproduce(IssueDetails.extractStepsToReproduce(driver)));
        issueTab.put("additional info", () -> issue.setAdditionalInformation(IssueDetails.extractAdditionalInformation(driver)));
        
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
                    throw new FieldNotFoundException("\"" + selected + "\" field not found.");
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
        List<Issue>      issues    = new ArrayList<>();
        
        driver.get("http://localhost/mantisbt/view_all_bug_page.php");
        
        // Show all projects
        selectProjectFilter(driver, 0);
        
        // Select number of issue per page
        if (pageSize != 50) {
            selectPageSize(driver, pageSize);
        }
        
        // check if page exists
        int totalIssues = getTotalIssues(driver);
        if (page != 1) {
            if ((page - 1) * pageSize > totalIssues) {
                System.err.println("Page " + page + " doesn't exist.");
                driver.quit();
                return issues;
            }
            driver.get("http://localhost/mantisbt/view_all_bug_page.php?page_number=" + page);
        }
        
        List<WebElement> issueRows = driver.findElement(By.id("buglist")).findElements(By.tagName("tr"));
        
        for(int i = 3 ; i < issueRows.size() - 1 ; i++) {
            issues.add(IssueRecap.extractAndSetFullRecap(issueRows.get(i)));
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
                IssueDetails.extractDescription(driver);
                // issue.setDescription();
            }
            else {
                driver.get("http://localhost/mantisbt/view_all_bug_page.php");
            }
            issues.add(issue);
        }
        
        driver.quit();
        return null;
    }
    
    private int getTotalIssues (WebDriver driver) {
        WebElement buglist = driver.findElement(By.id("buglist"));
        WebElement viewingIssues = buglist.findElement(By.xpath("//span[contains(text(),'Viewing Issues')]"));
        String temp = viewingIssues.getText().split("/ ")[1];
        return Integer.parseInt(temp.substring(0, temp.length() - 1));
    }
    
    private void selectPageSize (WebDriver driver, int pageSize) {
        driver.findElement(By.id("per_page_filter")).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        WebElement inputPageSize = driver.findElement(By.name("per_page"));
        inputPageSize.clear();
        inputPageSize.sendKeys(String.valueOf(pageSize));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        driver.findElement(By.name("filter")).click();
    }
    
    private void selectProjectFilter (WebDriver driver, int projectFilter) {
        WebElement selectProjectElement = driver.findElement(By.name("project_id"));
        Select selectProject = new Select(selectProjectElement);
        selectProject.selectByValue("0");
    }
}
