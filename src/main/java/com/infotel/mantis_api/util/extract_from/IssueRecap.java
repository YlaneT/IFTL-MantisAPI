package com.infotel.mantis_api.util.extract_from;

import com.infotel.mantis_api.model.Issue;
import org.openqa.selenium.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Class containing static methods to extract values from issue recap <br>
 * issue recap being {baseUrl}/view_all_bug_page.php
 */
public class IssueRecap {
    public static Issue extractAndSetFullRecap (WebElement issueRow, String projectName) {
        Issue issue = new Issue();
        
        List<WebElement> issueCol = issueRow.findElements(By.tagName("td"));
        
        extractAndSetPriority(issue, issueCol);
        extractAndSetId(issue, issueCol);
        extractAndSetNoteCount(issue, issueCol);
        extractAndSetAttachmentCount(issue, issueCol);
        extractAndSetCategory(issue, issueCol);
        extractAndSetSeverity(issue, issueCol);
        extractAndSetStatus(issue, issueCol);
        extractAndSetLastUpdated(issue, issueCol);
        extractAndSetSummary(issue, issueCol);
        
        issue.setProject(projectName);
        
        return issue;
    }
    
    public static void extractAndSetPriority (Issue issue, List<WebElement> issueCol) {
        try {
            issue.setPriority(issueCol.get(2).findElement(By.tagName("img")).getAttribute("title"));
        } catch (NoSuchElementException ignored) {
        }
    }
    
    public static void extractAndSetId (Issue issue, List<WebElement> issueCol) {
        issue.setId(issueCol.get(3).getText());
    }
    
    public static void extractAndSetNoteCount (Issue issue, List<WebElement> issueCol) {
        String noteCountStr = issueCol.get(4).getText();
        if (!noteCountStr.isBlank()) {
            issue.setNoteCount(Integer.parseInt(noteCountStr));
        }
    }
    
    public static void extractAndSetAttachmentCount (Issue issue, List<WebElement> issueCol) {
        String attachmentCountStr = issueCol.get(5).getText();
        if (!attachmentCountStr.isBlank()) {
            issue.setAttachmentCount(Integer.parseInt(attachmentCountStr));
        }
    }
    
    public static void extractAndSetCategory (Issue issue, List<WebElement> issueCol) {
        issue.setCategory(issueCol.get(6).getText());
    }
    
    public static void extractAndSetSeverity (Issue issue, List<WebElement> issueCol) {
        issue.setSeverity(issueCol.get(7).getText());
    }
    
    public static void extractAndSetStatus (Issue issue, List<WebElement> issueCol) {
        issue.setStatus(issueCol.get(8).getText());
    }
    
    public static void extractAndSetLastUpdated (Issue issue, List<WebElement> issueCol) {
        issue.setLastUpdated(
            LocalDate.parse(issueCol.get(9).getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());
    }
    
    public static void extractAndSetSummary (Issue issue, List<WebElement> issueCol) {
        issue.setSummary(issueCol.get(10).getText());
    }
}
