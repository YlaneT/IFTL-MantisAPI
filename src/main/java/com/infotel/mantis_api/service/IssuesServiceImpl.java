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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class IssuesServiceImpl implements IssuesService {
    
    @Autowired
    Authenticator auth;
    @Value("${mantis.base-url}")
    private String baseUrl;
    
    @Override
    public Issue searchIssue (int id) throws IssueNotFoundException {
        WebDriver driver = auth.login();
        Issue     issue  = new Issue();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        
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
        WebDriver driver = auth.login();
        Issue     issue  = new Issue();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        
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
        issueTab.put("additional info",
            () -> issue.setAdditionalInformation(IssueDetails.extractAdditionalInformation(driver)));
        
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
                    String     xPath              = "//td[text()='" + selected + "' and @class='category']";
                    WebElement customFieldElem    = driver.findElement(By.xpath(xPath));
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
    @Override
    public List<Issue> searchAllIssues (int pageSize, int page) {
        WebDriver driver = auth.login();
        List<Issue>      issues    = new ArrayList<>();
        
        driver.get(baseUrl + "/view_all_bug_page.php");
        
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
            driver.get(baseUrl + "/view_all_bug_page.php?page_number=" + page);
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
    public List<Issue> searchAllIssues (int pageSize, int page, List<String> selectValues) throws FieldNotFoundException {
        WebDriver driver = auth.login();
        
        driver.get(baseUrl + "/view_all_bug_page.php");
        
        WebElement       buglist   = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<Issue>      issues    = new ArrayList<>();
        
        for(int i = 3 ; i < issueRows.size() - 1 ; i++) {
            WebElement       issueRow = issueRows.get(i);
            List<WebElement> issueCol = issueRow.findElements(By.tagName("td"));
            
            Issue issue  = new Issue();
            int   fields = 0;
            if (selectValues.contains("id")) {
                IssueRecap.extractAndSetId(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("priority")) {
                IssueRecap.extractAndSetPriority(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("note count")) {
                IssueRecap.extractAndSetNoteCount(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("attachment count")) {
                IssueRecap.extractAndSetAttachmentCount(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("category")) {
                IssueRecap.extractAndSetCategory(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("severity")) {
                IssueRecap.extractAndSetSeverity(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("status")) {
                IssueRecap.extractAndSetStatus(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("updated")) {
                IssueRecap.extractAndSetLastUpdated(issue, issueCol);
                fields++;
            }
            if (selectValues.contains("summary")) {
                IssueRecap.extractAndSetSummary(issue, issueCol);
                fields++;
            }
            
            if (fields == 0) {
                driver.quit();
                throw new FieldNotFoundException("Field(s) " + String.join(", ", selectValues) + " not found");
            }
            issues.add(issue);
        }
        
        driver.quit();
        return issues;
    }

    public void createIssue(String category, String reproducibility, String severity,
                            String priority, String platform, String os,
                            String osVersion, String assigned, String summary, String description,
                            String stepsToReproduce, String additionalInformation) throws FieldNotFoundException {

        WebDriver driver = auth.login();
        driver.get("http://localhost/mantisbt/bug_report_page.php");

        WebElement dropdownCat = driver.findElement(By.name("category_id"));
        Select dropDown = new Select(dropdownCat);

        //valeur par defaut
        if (category.isEmpty()) {
            dropDown.selectByValue("1");
        } else {
            dropDown.selectByVisibleText(category);
        }

        WebElement drpReproducibility = driver.findElement(By.name("reproducibility"));
        Select dropdown = new Select(drpReproducibility);

        if (reproducibility != null && !reproducibility.equals("have not tried")) {
            dropdown.selectByVisibleText(reproducibility);
        }

        WebElement drpSeverity = driver.findElement(By.name("severity"));
        Select drop_down = new Select(drpSeverity);

        if (severity != null && !severity.equals("minor")) {
            drop_down.selectByVisibleText(severity);
        }

        WebElement drpPriority = driver.findElement(By.name("priority"));
        Select drp_down = new Select(drpPriority);

        if (priority != null && !priority.equals("normal")) {
            drp_down.selectByVisibleText(priority);
        }

        WebElement platformField = driver.findElement(By.id("platform"));

        if (platform != null && !platform.isEmpty()) {
            platformField.sendKeys(platform);
        }

        WebElement osField = driver.findElement(By.id("os"));

        if (os != null && !os.isEmpty()) {
            osField.sendKeys(os);
        }

        WebElement osVersionField = driver.findElement(By.id("os_build"));

        if (osVersion != null && !osVersion.isEmpty()) {
            osVersionField.sendKeys(osVersion);
        }

        // TODO: Assigned

        WebElement drpAssigned = driver.findElement(By.name("handler_id"));
        Select drpDown = new Select(drpAssigned);

        if (assigned == null || assigned.isEmpty()) {
            drpDown.selectByValue("0");
        } else {
            drpDown.selectByVisibleText(assigned);
        }

        WebElement summaryField = driver.findElement(By.name("summary"));

        if (summary != null && !summary.isEmpty()) {
            summaryField.sendKeys(description);
        } else {
            // TODO: throw new FieldNotFoundException("Description empty / not found")
            // driver.get("http://localhost/mantisbt/bug_report_page.php");
            Exception e = new Exception();
            throw new FieldNotFoundException("Summary empty / not found", e);
        }

        WebElement descriptionField = driver.findElement(By.name("description"));

        if (description != null && !description.isEmpty()) {
            descriptionField.sendKeys(description);
        } else {
            // TODO: throw new FieldNotFoundException("Description empty / not found")
            // driver.get("http://localhost/mantisbt/bug_report_page.php");
            Exception e = new Exception();
            throw new FieldNotFoundException("Description empty / not found", e);
        }

        WebElement stepstoreproduceField = driver.findElement(By.name("steps_to_reproduce"));

        if (stepsToReproduce != null && !stepsToReproduce.isEmpty()) {
            stepstoreproduceField.sendKeys(stepsToReproduce);
        }

        WebElement additionalField = driver.findElement(By.name("additional_info"));

        if (additionalInformation != null && !additionalInformation.isEmpty()) {
            additionalField.sendKeys(additionalInformation);
        }

        WebElement submitButton = driver.findElement(By.className("button"));
        submitButton.click();

    }


    public void addNote(int id, String note) {

        WebDriver driver = auth.login();

        driver.get("http://localhost/mantisbt/view.php?id=" + id);
        driver.findElement(By.name("bugnote_text")).sendKeys(note);

        WebElement noteBtn = driver.findElement(By.xpath("//input[@value='Add Note']"));
        noteBtn.click();

        driver.quit();
    }


    private void extractAllMandatoryFields(Issue issue, WebDriver driver) {
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
            String xPath = "//td[text()='Steps To Reproduce' and @class='category']";
            WebElement strTitle = driver.findElement(By.xpath(xPath));
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
                driver.findElement(By.xpath("//td[text()='Additional Information' and " + "@class" + "='category']"));
            WebElement strElement = aiTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
            return null;
        }
    }
    
    private int getTotalIssues (WebDriver driver) {
        WebElement buglist       = driver.findElement(By.id("buglist"));
        WebElement viewingIssues = buglist.findElement(By.xpath("//span[contains(text(),'Viewing Issues')]"));
        String     temp          = viewingIssues.getText().split("/ ")[1];
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
        Select     selectProject        = new Select(selectProjectElement);
        selectProject.selectByValue("0");
    }
    
    private static LocalDateTime parseDate (String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
