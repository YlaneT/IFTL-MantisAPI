package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        WebElement tagsElement = driver.findElement(By.xpath("//table[3]/tbody/tr[6]/td[2]"));
        if (!tagsElement.getText().equals("No tags attached.")) {
            // Recherche de l'élément contenant le texte "Tags"
            WebElement tagsHeaderElement = driver.findElement(By.xpath("//td[text()='Tags']"));

            // Trouver l'élément suivant immédiat de la cellule (la case du tableau à droite de "Tags")
            WebElement tagsValueElement = tagsHeaderElement.findElement(By.xpath("./following-sibling::td"));
            List<WebElement> links = tagsValueElement.findElements(By.cssSelector("a"));

            for (int i = 0; i < links.size(); i += 2) {
                issue.getTags().add(links.get(i).getText()); // Ajoute chaque tag à l'issue
            }
        }

        try {
            WebElement strTitle = driver.findElement(By.xpath("//td[text()='Steps To Reproduce' and @class='category']"));
            WebElement strElement = strTitle.findElement(By.xpath("./following-sibling::td"));
            String stepsToReproduce = strElement.getText();
            issue.setStepsToReproduce(stepsToReproduce);
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
        }

        try {
            WebElement aiTitle = driver.findElement(By.xpath("//td[text()='Additional Information']"));
            WebElement strElement = aiTitle.findElement(By.xpath("./following-sibling::td"));
            String stepsToReproduce = strElement.getText();
            issue.setAdditionalInformation(stepsToReproduce);
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
        }

        driver.quit();
        return issue;
    }


    private List<Issue> searchAllIssues() {
        return searchAllIssues(10, 1);
    }

    /**
     * @param pageSize number of issues per page
     * @param page number of the page shown
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


            String date = buglist.findElement(By.xpath("//tr["+ (i+1) + "]/td[" + 10 + "]")).getText();
            issue.setLastUpdated(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());

            issues.add(issue);
            // TODO: Gérer la pagination à nouveau
        }
        return issues;
    }

    /**
     * @param pageSize number of issues per page
     * @param page number of the page shown
     * @param selectValues fields to be shown
     * @return selected fields of all issues on the page
     */
    public List<Issue> searchAllIssues(int pageSize, int page, List<String> selectValues) {
        if (selectValues.isEmpty()){
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

            if(selectValues.contains("id")) {
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


        return null;
    }

    private String extractDescription (WebDriver driver) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[12]/td[2]")).getText();
    }

    private static LocalDateTime parseDate(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
