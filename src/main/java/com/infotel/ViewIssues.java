package com.infotel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ViewIssues {

    public static List<IssueDto> getAllIssues(WebDriver driver) {

        driver.get("http://localhost/mantisbt/view_all_bug_page.php");

        WebElement buglist = driver.findElement(By.id("buglist"));
        List<WebElement> issueRows = buglist.findElements(By.tagName("tr"));
        List<IssueDto> issues = new ArrayList<>();

        for (int i = 3; i < issueRows.size() - 1; i++) {
            WebElement issueRow = issueRows.get(i);
            IssueDto issue = new IssueDto();

            List<WebElement> columns = issueRow.findElements(By.tagName("td"));
            List<String> strColumns = new ArrayList<>();

            for (WebElement col : columns) {
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
