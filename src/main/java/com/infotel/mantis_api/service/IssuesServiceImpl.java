package com.infotel.mantis_api.service;

import com.infotel.mantis_api.endpoint.IssueFilesController;
import com.infotel.mantis_api.exception.*;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.util.Authenticator;
import com.infotel.mantis_api.util.extract_from.IssueDetails;
import com.infotel.mantis_api.util.extract_from.IssueRecap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

import static com.infotel.mantis_api.util.edit.EditIssue.*;


@Service
public class IssuesServiceImpl implements IssuesService {
    
    private static final int    MAX_CUSTOM_LENGTH     = 255;
    private static final int    MAX_OS_LENGTH         = 32;
    private static final int    MAX_OS_VERSION_LENGTH = 16;
    private static final int    MAX_PLATFORM_LENGTH   = 32;
    private static final int    MAX_SUMMARY_LENGTH    = 128;
    private final        Logger log                   = LogManager.getLogger(IssueFilesController.class);
    @Autowired
    Authenticator auth;
    @Value("${mantis.base-url}")
    private String baseUrl;
    
    @Override
    public Issue searchIssue (int id) throws IssueNotFoundException, AccessDenied {
        WebDriver driver = auth.login();
        Issue     issue  = new Issue();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        
        try {
            driver.findElement(By.xpath("//body/center/p[text()='Access Denied.']"));
            driver.quit();
            throw new AccessDenied("User doesn't have permission to view this issue.");
        } catch (NoSuchElementException ignore) {}
        
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
    public Issue searchIssue (int id, List<String> selectValues)
        throws IssueNotFoundException, FieldNotFoundException, AccessDenied {
        WebDriver driver = auth.login();
        Issue     issue  = new Issue();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        
        try {
            driver.findElement(By.xpath("//body/center/p[text()='Access Denied.']"));
            driver.quit();
            throw new AccessDenied("User doesn't have permission to view this issue.");
        } catch (NoSuchElementException ignore) {}
        
        Map<String, Runnable> issueTab = Map.ofEntries(
            Map.entry("id", () -> issue.setId(IssueDetails.extractId(driver))),
            Map.entry("project", () -> issue.setProject(IssueDetails.extractProject(driver))),
            Map.entry("category", () -> issue.setCategory(IssueDetails.extractCategory(driver))),
            Map.entry("view status", () -> issue.setViewStatus(IssueDetails.extractViewStatus(driver))),
            Map.entry("submitted", () -> issue.setSubmitted(IssueDetails.extractSubmitted(driver))),
            Map.entry("updated", () -> issue.setLastUpdated(IssueDetails.extractUpdated(driver))),
            Map.entry("reporter", () -> issue.setReporter(IssueDetails.extractReporter(driver))),
            Map.entry("assigned", () -> issue.setAssigned(IssueDetails.extractAssigned(driver))),
            Map.entry("priority", () -> issue.setPriority(IssueDetails.extractPriority(driver))),
            Map.entry("severity", () -> issue.setSeverity(IssueDetails.extractSeverity(driver))),
            Map.entry("reproducibility", () -> issue.setReproducibility(IssueDetails.extractReproducibility(driver))),
            Map.entry("status", () -> issue.setStatus(IssueDetails.extractStatus(driver))),
            Map.entry("resolution", () -> issue.setResolution(IssueDetails.extractResolution(driver))),
            Map.entry("platform", () -> issue.setPlatform(IssueDetails.extractPlatform(driver))),
            Map.entry("os", () -> issue.setOs(IssueDetails.extractOs(driver))),
            Map.entry("os version", () -> issue.setOsVersion(IssueDetails.extractOsVersion(driver))),
            Map.entry("summary", () -> issue.setSummary(IssueDetails.extractSummary(driver))),
            Map.entry("description", () -> issue.setDescription(IssueDetails.extractDescription(driver))),
            Map.entry("tags", () -> issue.setTags(IssueDetails.extractTags(driver))),
            Map.entry("steps", () -> issue.setStepsToReproduce(IssueDetails.extractStepsToReproduce(driver))),
            Map.entry("additional info",
                () -> issue.setAdditionalInformation(IssueDetails.extractAdditionalInformation(driver)))
        );
        
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
            } else {
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
    
    /**
     * @param pageSize     number of issues per page
     * @param page         number of the page shown
     * @param selectValues fields to be shown
     * @param projectId    project to filter by
     * @return selected fields of all issues on the page
     */
    @Override
    public List<Issue> searchAllIssues (
        int pageSize, int page, List<String> selectValues, int projectId
    ) throws FieldNotFoundException, ProjectNotFoundException {
        WebDriver   driver = auth.login();
        List<Issue> issues = new ArrayList<>();
        
        driver.get(baseUrl + "/view_all_bug_page.php");
        
        // Select project
        String projectName;
        try {
            projectName = displayFilteredProjectIssues(driver, pageSize, page, projectId);
        } catch (PageDoesNotExistException e) {
            log.info(e.getMessage());
            driver.quit();
            return issues;
        }
        
        WebElement       buglist   = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        
        for(int i = 3 ; i < issueRows.size() - 1 ; i++) {
            WebElement issueRow = issueRows.get(i);
            if (issueRow.findElements(By.tagName("td")).size() != 1) {
                
                if (selectValues.size() == 0) {
                    issues.add(IssueRecap.extractAndSetFullRecap(issueRow, projectName));
                } else {
                    List<WebElement> issueCol = issueRow.findElements(By.tagName("td"));
                    
                    Issue issue = new Issue();
                    issue.setProject(projectName);
                    int fields = 0;
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
                    
                    if (fields == 0 && projectName == null) {
                        driver.quit();
                        throw new FieldNotFoundException("Field(s) " + String.join(", ", selectValues) + " not found");
                    }
                    issues.add(issue);
                }
            }
        }
        
        driver.quit();
        return issues;
    }
    
    @Override
    public String editIssue (int issue_id, Issue issue) throws IssueNotFoundException, AccessDenied {
        if (issue.getId() == null) {
            throw new IssueNotFoundException("Issue id is null");
        }
        
        Issue        oldIssue      = searchIssue(issue_id);
        String       returnMessage = "Issue " + issue_id + " was edited";
        List<String> errors        = new ArrayList<>();
        
        WebDriver driver = auth.login();
        driver.get(baseUrl + "/view.php?id=" + issue_id);
        // View permissions already verified from using searchIssue()
        
        Class<? extends Issue> clazz  = issue.getClass();
        Field[]                fields = clazz.getDeclaredFields();
        
        try {
            extractEditButton(driver).click();
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new AccessDenied("User doesn't have permission to edit issue.");
        }
        
        try {
            for(Field field : fields) {
                field.setAccessible(true);
                if (isNotNullAndDifferent(oldIssue, issue, field)) {
                    String fieldName = field.getName();
                    List<String> editable = Arrays.asList("category", "viewStatus", "reporter", "assigned", "priority",
                        "severity", "reproducibility", "status", "resolution", "platform", "os", "osVersion", "summary",
                        "description", "stepsToReproduce", "additionalInformation", "customFields");
                    if (editable.contains(fieldName)) {
                        String content = fieldName.equals("customFields") ? "" : (String) field.get(issue);
                        switch (fieldName) {
                            case "category" -> {
                                try {
                                    editCategory(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "viewStatus" -> {
                                try {
                                    editViewStatus(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "reporter" -> {
                                try {
                                    editReporter(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "assigned" -> {
                                try {
                                    editAssigned(driver, content);
                                } catch (FieldNotFoundException | AccessDenied e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "priority" -> {
                                try {
                                    editPriority(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "severity" -> {
                                try {
                                    editSeverity(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "reproducibility" -> {
                                try {
                                    editReproducibility(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "status" -> {
                                try {
                                    editStatus(driver, content);
                                } catch (FieldNotFoundException | AccessDenied e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "resolution" -> {
                                try {
                                    editResolution(driver, content);
                                } catch (FieldNotFoundException e) {
                                    errors.add(e.getMessage());
                                    log.warn(e.getMessage());
                                }
                            }
                            case "platform" -> {
                                if ((content).length() > MAX_PLATFORM_LENGTH) {
                                    errors.add("Platform too long (max " + MAX_PLATFORM_LENGTH + " characters)");
                                }
                                editPlatform(driver, verifyInput(content, MAX_PLATFORM_LENGTH));
                            }
                            case "os" -> {
                                if ((content).length() > MAX_OS_LENGTH) {
                                    errors.add("OS too long (max " + MAX_OS_LENGTH + " characters)");
                                }
                                editOs(driver, verifyInput(content, MAX_PLATFORM_LENGTH));
                            }
                            case "osVersion" -> {
                                if (content.length() > MAX_OS_VERSION_LENGTH) {
                                    errors.add("OS version too long (max " + MAX_OS_VERSION_LENGTH + " characters)");
                                }
                                editOsVersion(driver, verifyInput(content, MAX_OS_VERSION_LENGTH));
                            }
                            case "summary" -> {
                                if (content.length() > MAX_SUMMARY_LENGTH) {
                                    errors.add("Summary too long (max " + MAX_SUMMARY_LENGTH + " characters)");
                                }
                                editSummary(driver, verifyInput(content, MAX_SUMMARY_LENGTH));
                            }
                            case "description" -> editDescription(driver, content);
                            case "stepsToReproduce" -> editStepsToReproduce(driver, content);
                            case "additionalInformation" -> editAdditionalInformation(driver, content);
                            case "customFields" -> {
                                Map<String, String> customFields = (Map<String, String>) field.get(issue);
                                for(String key : customFields.keySet()) {
                                    String value = customFields.get(key);
                                    try {
                                        editCustomField(driver, key, verifyInput(value, MAX_CUSTOM_LENGTH));
                                        if (value.length() > MAX_CUSTOM_LENGTH) {
                                            errors.add(
                                                "Custom field \"" + key + "\" too long (max " + MAX_CUSTOM_LENGTH +
                                                    " characters)");
                                        }
                                    } catch (FieldNotFoundException e) {
                                        errors.add(e.getMessage());
                                        log.warn(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            driver.quit();
            e.printStackTrace();
        }
        
        extractUpdateButton(driver).click();
        
        driver.quit();
        if (!errors.isEmpty()) {
            returnMessage = returnMessage + " with errors: " + String.join(", ", errors);
        }
        return returnMessage;
    }
    
    @Override
    public String createIssue (
        String project, String category, String reproducibility, String severity, String priority, String platform,
        String os, String osVersion, String assigned, String summary, String description, String stepsToReproduce,
        String additionalInformation
    ) throws FieldNotFoundException, AccessDenied, ProjectNotFoundException, AmbiguousProjectException {
        
        if (project == null || project.isEmpty() || category == null || summary == null || description == null) {
            String message = "project, category, summary or description was not found";
            log.warn(message);
            throw new FieldNotFoundException(message);
        }
        
        WebDriver driver = auth.login();
        
        driver.get(baseUrl + "/bug_report_page.php");
        
        // Check if access denied
        try {
            driver.findElement(By.xpath("//body/center/p[text()='Access Denied.']"));
            driver.quit();
            String message = "User doesn't have permission to create an issue.";
            log.warn(message);
            throw new AccessDenied(message);
        } catch (NoSuchElementException ignore) {}
        
        // Check if popup
        try {
            String     xPath = "//body/div/form[@action='set_project.php']";
            WebElement form  = driver.findElement(By.xpath(xPath));
            
            Select setProjectSelect = new Select(form.findElement(By.name("project_id")));
            setProjectSelect.selectByVisibleText(project);
            driver.findElement(By.xpath("//input[@value='Select Project']")).click();
        } catch (NoSuchElementException e) {
            // If not popup: check for project_id
            WebElement inputOrSelect = driver.findElement(By.name("project_id"));
            if (inputOrSelect.getAttribute("type").equals("hidden")) {
                // If hidden: Project not found
                String message = "Project of created issue is ambiguous";
                log.warn(message);
                throw new AmbiguousProjectException(message);
            } else {
                try {
                    Select selectProject = new Select(inputOrSelect);
                    selectProject.selectByVisibleText(project);
                } catch (NoSuchElementException ex) {
                    driver.quit();
                    String message = "Project \"%s\" was not found".formatted(project);
                    log.warn(message);
                    throw new ProjectNotFoundException(message);
                }
            }
        }
        
        
        // Check if access denied
        try {
            driver.findElement(By.xpath("//body/center/p[text()='Access Denied.']"));
            driver.quit();
            throw new AccessDenied("User doesn't have permission to create an issue for this project.");
        } catch (NoSuchElementException ignore) {}
        
        
        WebElement dropdownCat = driver.findElement(By.name("category_id"));
        Select     dropDown    = new Select(dropdownCat);
        
        String       returnMessage = "Issue was created";
        List<String> errors        = new ArrayList<>();
        
        try {
            if (category.isEmpty()) {
                driver.quit();
                throw new FieldNotFoundException("Category is empty");
            }
            dropDown.selectByVisibleText(category);
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new FieldNotFoundException("Category \"%s\" doesn't exist".formatted(category));
        }
        
        WebElement drpReproducibility = driver.findElement(By.name("reproducibility"));
        Select     dropdown           = new Select(drpReproducibility);
        
        try {
            if (reproducibility != null && !reproducibility.equals("have not tried")) {
                dropdown.selectByVisibleText(reproducibility);
            }
        } catch (NoSuchElementException e) {
            String e_msg = "Reproducibility \"%s\" doesn't exist".formatted(reproducibility);
            errors.add(e_msg);
            log.warn(e_msg);
        }
        
        WebElement drpSeverity = driver.findElement(By.name("severity"));
        Select     drop_down   = new Select(drpSeverity);
        
        try {
            if (severity != null && !severity.equals("minor")) {
                drop_down.selectByVisibleText(severity);
            }
        } catch (NoSuchElementException e) {
            String e_msg = "Severity \"%s\" doesn't exist".formatted(severity);
            errors.add(e_msg);
            log.warn(e_msg);
        }
        
        WebElement drpPriority = driver.findElement(By.name("priority"));
        Select     drp_down    = new Select(drpPriority);
        
        try {
            if (priority != null && !priority.equals("normal")) {
                drp_down.selectByVisibleText(priority);
            }
        } catch (NoSuchElementException e) {
            String e_msg = "Priority \"%s\" doesn't exist".formatted(priority);
            errors.add(e_msg);
            log.warn(e_msg);
        }
        
        if (platform != null && !platform.isEmpty()) {
            if (platform.length() > MAX_PLATFORM_LENGTH) {
                String e_msg = "Platform too long (max " + MAX_PLATFORM_LENGTH + " characters)";
                errors.add(e_msg);
                log.warn(e_msg);
            }
            editPlatform(driver, verifyInput(platform, MAX_PLATFORM_LENGTH));
        }
        
        if (os != null && !os.isEmpty()) {
            if (os.length() > MAX_OS_LENGTH) {
                String e_msg = "OS too long (max " + MAX_OS_LENGTH + " characters)";
                errors.add(e_msg);
                log.warn(e_msg);
            }
            editOs(driver, verifyInput(os, MAX_PLATFORM_LENGTH));
        }
        
        if (osVersion != null && !osVersion.isEmpty()) {
            if (osVersion.length() > MAX_OS_VERSION_LENGTH) {
                String e_msg = "OS version too long (max " + MAX_OS_VERSION_LENGTH + " characters)";
                errors.add(e_msg);
                log.warn(e_msg);
            }
            editOsVersion(driver, verifyInput(osVersion, MAX_OS_VERSION_LENGTH));
        }
        
        if (assigned == null || assigned.isEmpty()) {
            new Select(driver.findElement(By.name("handler_id"))).selectByValue("0");
        } else {
            try {
                editAssigned(driver, assigned);
            } catch (FieldNotFoundException | AccessDenied e) {
                errors.add(e.getMessage());
                log.warn(e.getMessage());
            }
        }
        
        if (summary.isEmpty()) {
            driver.quit();
            throw new FieldNotFoundException("Summary empty or not found");
        }
        
        if (summary.length() > MAX_SUMMARY_LENGTH) {
            String e_msg = "Summary too long (max " + MAX_SUMMARY_LENGTH + " characters)";
            errors.add(e_msg);
            log.warn(e_msg);
        }
        editSummary(driver, verifyInput(summary, MAX_SUMMARY_LENGTH));
        
        if (description.isEmpty()) {
            driver.quit();
            throw new FieldNotFoundException("Description empty or not found");
        }
        driver.findElement(By.name("description")).sendKeys(description);
        
        if (stepsToReproduce != null && !stepsToReproduce.isEmpty()) {
            editStepsToReproduce(driver, stepsToReproduce);
        }
        
        if (additionalInformation != null && !additionalInformation.isEmpty()) {
            driver.findElement(By.name("additional_info")).sendKeys(additionalInformation);
        }
        
        driver.findElement(By.xpath("//input[@value='Submit Report']")).click();
        
        driver.quit();
        if (!errors.isEmpty()) {
            returnMessage = returnMessage + " with errors: " + String.join(", ", errors);
        }
        return returnMessage;
    }
    
    @Override
    public void addNote (int id, String note) {
        
        WebDriver driver = auth.login();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        driver.findElement(By.name("bugnote_text")).sendKeys(note);
        
        WebElement noteBtn = driver.findElement(By.xpath("//input[@value='Add Note']"));
        noteBtn.click();
        
        driver.quit();
    }
    
    private String displayFilteredProjectIssues (WebDriver driver, int pageSize, int page, int projectId) throws ProjectNotFoundException, PageDoesNotExistException {
        // Select project
        String projectName = selectProjectFilter(driver, projectId);
        
        // Select number of issue per page
        selectPageSize(driver, pageSize);
        
        // check if page exists
        int totalIssues = getTotalIssues(driver);
        if (page != 1) {
            if ((page - 1) * pageSize > totalIssues) {
                throw new PageDoesNotExistException("Page " + page + " doesn't exist.");
            }
            driver.get(baseUrl + "/view_all_bug_page.php?page_number=" + page);
        }
        return projectName;
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
    
    private String selectProjectFilter (WebDriver driver, int projectFilter) throws ProjectNotFoundException {
        WebElement selectProjectElement;
        try {
            selectProjectElement = driver.findElement(By.name("project_id"));
        } catch (NoSuchElementException e) {
            driver.findElement(By.xpath("//a[text()='Advanced Filters']")).click();
            driver.findElement(By.id("project_id_filter")).click();
            WebElement selectPjtTd = driver.findElement(By.id("project_id_filter_target"));
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                driver.quit();
                throw new RuntimeException(ie);
            }
            selectProjectElement = selectPjtTd.findElement(By.xpath("//select"));
        }
        
        Select selectProject = new Select(selectProjectElement);
        String projectName   = null;
        if (projectFilter != 0) {
            List<WebElement> options = selectProject.getOptions();
            for(WebElement opt : options) {
                if (opt.getAttribute("value").equals(String.valueOf(projectFilter))) {
                    projectName = opt.getText();
                    break;
                }
            }
        }
        
        try {
            selectProject.selectByValue(String.valueOf(projectFilter));
            return projectName;
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new ProjectNotFoundException("Project with id " + projectFilter + " not found");
        }
    }
    
    private boolean isNotNullAndDifferent (Issue old, Issue edit, Field field) {
        try {
            return field.get(edit) != null && !field.get(edit).equals(field.get(old));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
