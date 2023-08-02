package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.FieldNotFoundException;
import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IssuesServiceImpl implements IssuesService {

    @Override
    public Issue searchIssue(int id) {
        WebDriver driver = Authenticator.login();
        Issue issue = new Issue();

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
    public Issue searchIssue(int id, List<String> selectValues) {
        WebDriver driver = Authenticator.login();
        Issue issue = new Issue();

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

        for (String selected : selectValues) {
            if (issueTab.containsKey(selected.toLowerCase())) {
                issueTab.get(selected.toLowerCase());
            } else {
                try {
                    WebElement customFieldElem =
                            driver.findElement(By.xpath("//td[text()='" + selected + "' and @class='category']"));
                    WebElement customFieldValElem =
                            customFieldElem.findElement(By.xpath("./following-sibling::td"));

                    issue.getCustomFields().put(customFieldElem.getText(), customFieldValElem.getText());
                } catch (NoSuchElementException e) {
                    // TODO: Throw exception Issue field not found
                    System.err.println("\"" + selected + "\" field not found.");
                }
            }
        }
        driver.quit();
        return issue;
    }

    private List<Issue> searchAllIssues() {
        return searchAllIssues(10, 1);
    }

    /**
     * @param pageSize number of issues per page
     * @param page     number of the page shown
     * @return recap of all issues on the page
     */
    private List<Issue> searchAllIssues(int pageSize, int page) {
        WebDriver driver = Authenticator.login();

        driver.get("http://localhost/mantisbt/view_all_bug_page.php");

        WebElement buglist = driver.findElement(By.id("buglist"));

        // TODO: Gérer la pagination

        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<Issue> issues = new ArrayList<>();

        for (int i = 3; i < issueRows.size() - 1; i++) {
            WebElement issueRow = issueRows.get(i);
            Issue issue = new Issue();

            List<WebElement> columns = issueRow.findElements(By.tagName("td"));
            List<String> strColumns = new ArrayList<>();

            for (WebElement col : columns) {
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
    public List<Issue> searchAllIssues(int pageSize, int page, List<String> selectValues) {
        if (selectValues.isEmpty()) {
            return searchAllIssues(pageSize, page);
        }

        // FIXME
        WebDriver driver = Authenticator.login();

        driver.get("http://localhost/mantisbt/view_all_bug_page.php");

        WebElement buglist = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<Issue> issues = new ArrayList<>();

        /* For row in issueRows
         *      ajouter une issue
         *      tester les selectValues
         *      si elles contiennent "id" :
         *          issue.setId( __ )
         *      si elles contiennent "summary" :
         *          issue.setSummary ( __ )
         *      si elles contiennent "description" :
         *          cliquer sur l'id pour accéder au détail
         *          issue.setDescription ( __ )
         *          revenir à la page view_all_bug_page
         *      ajouter issue à la liste issues
         */

        for (WebElement row : issueRows) {
            Issue issue = new Issue();

            if (selectValues.contains("id")) {
                WebElement issueId = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/td[4]"));
                issueId.getText();
                // issue.setId();
            } else if (selectValues.contains("summary")) {
                WebElement summaryIssue = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/tr[11]"));
                summaryIssue.getText();
                //  issue.setSummary();
            } else if (selectValues.contains("description")) {
                WebElement issueDetail = driver.findElement(By.xpath("//table[3]/tbody/tr[4]/td[4]"));
                issueDetail.click();
                extractDescription(driver);
                // issue.setDescription();
            } else {
                driver.get("http://localhost/mantisbt/view_all_bug_page.php");
            }
            issues.add(issue);
        }

        driver.quit();
        return null;
    }

    public void createIssue(String category, String reproducibility, String severity,
                            String priority, String platform, String os,
                            String osVersion, String assigned, String summary, String description,
                            String stepsToReproduce, String additionalInformation) throws FieldNotFoundException {

        WebDriver driver = Authenticator.login();
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

        WebDriver driver = Authenticator.login();

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

    private void extractAllOptionalFields(Issue issue, WebDriver driver) {
        issue.setStepsToReproduce(extractStepsToReproduce(driver));
        issue.setAdditionalInformation(extractAdditionalInformation(driver));
    }

    private String extractFromIssueTab(WebDriver driver, int x, int y) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[" + x + "]/td[" + y + "]")).getText();
    }

    private String extractId(WebDriver driver) {
        return extractFromIssueTab(driver, 3, 1);
    }

    private String extractProject(WebDriver driver) {
        return extractFromIssueTab(driver, 3, 2);
    }

    private String extractCategory(WebDriver driver) {
        return extractFromIssueTab(driver, 3, 3);
    }

    private String extractViewStatus(WebDriver driver) {
        return extractFromIssueTab(driver, 3, 4);
    }

    private LocalDateTime extractSubmitted(WebDriver driver) {
        String submittedStr = extractFromIssueTab(driver, 3, 5);
        return parseDate(submittedStr);
    }

    private LocalDateTime extractUpdated(WebDriver driver) {
        String updatedStr = extractFromIssueTab(driver, 3, 6);
        return parseDate(updatedStr);
    }

    private String extractReporter(WebDriver driver) {
        return extractFromIssueTab(driver, 5, 2);
    }

    private String extractAssigned(WebDriver driver) {
        return extractFromIssueTab(driver, 6, 2);
    }

    private String extractPriority(WebDriver driver) {
        return extractFromIssueTab(driver, 7, 2);
    }

    private String extractSeverity(WebDriver driver) {
        return extractFromIssueTab(driver, 7, 4);
    }

    private String extractReproducibility(WebDriver driver) {
        return extractFromIssueTab(driver, 7, 6);
    }

    private String extractStatus(WebDriver driver) {
        return extractFromIssueTab(driver, 8, 2);
    }

    private String extractResolution(WebDriver driver) {
        return extractFromIssueTab(driver, 8, 4);
    }

    private String extractPlatform(WebDriver driver) {
        return extractFromIssueTab(driver, 9, 2);
    }

    private String extractOs(WebDriver driver) {
        return extractFromIssueTab(driver, 9, 4);
    }

    private String extractOsVersion(WebDriver driver) {
        return extractFromIssueTab(driver, 9, 6);
    }

    private String extractSummary(WebDriver driver) {
        return extractFromIssueTab(driver, 11, 2);
    }

    private String extractDescription(WebDriver driver) {
        return extractFromIssueTab(driver, 12, 2);
    }

    private List<String> extractTags(WebDriver driver) {
        WebElement tagsCategoryElement = driver.findElement(By.xpath("//td[text()='Tags' and @class='category']"));
        // Find immediate sibling of Tags header
        WebElement tagsValueElement = tagsCategoryElement.findElement(By.xpath("./following-sibling::td"));
        List<String> tags = new ArrayList<>();
        if (!tagsValueElement.getText().equals("No tags attached.")) {
            // Extract links containing text, not delete cross
            List<WebElement> links = tagsValueElement.findElements(By.cssSelector("a"));
            for (int i = 0; i < links.size(); i += 2) {
                tags.add(links.get(i).getText());
            }
        }
        return tags;
    }


    private String extractStepsToReproduce(WebDriver driver) {
        try {
            WebElement strTitle =
                    driver.findElement(By.xpath("//td[text()='Steps To Reproduce' and " + "@class='category']"));
            WebElement strElement = strTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
            return null;
        }
    }

    private String extractAdditionalInformation(WebDriver driver) {
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

    private static LocalDateTime parseDate(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
