package com.infotel.mantis_api.util.extract_from;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.model.Issue;
import org.openqa.selenium.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing static methods to extract values from issue details <br>
 * issue details being localhost/mantisbt/view.php?id={id}
 */
public class IssueDetails {
    public static void extractAndSetAllMandatoryFields (Issue issue, WebDriver driver) {
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
    
    public static void extractAndSetAllOptionalFields (Issue issue, WebDriver driver) {
        issue.setStepsToReproduce(extractStepsToReproduce(driver));
        issue.setAdditionalInformation(extractAdditionalInformation(driver));
    }
    
    public static String extractFromIssueTab (WebDriver driver, int x, int y) {
        return driver.findElement(By.xpath("//table[3]/tbody/tr[" + x + "]/td[" + y + "]")).getText();
    }
    
    public static String extractId (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 1);
    }
    
    public static String extractProject (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 2);
    }
    
    public static String extractCategory (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 3);
    }
    
    public static String extractViewStatus (WebDriver driver) {
        return extractFromIssueTab(driver, 3, 4);
    }
    
    public static LocalDateTime extractSubmitted (WebDriver driver) {
        String submittedStr = extractFromIssueTab(driver, 3, 5);
        return parseDate(submittedStr);
    }
    
    public static LocalDateTime extractUpdated (WebDriver driver) {
        String updatedStr = extractFromIssueTab(driver, 3, 6);
        return parseDate(updatedStr);
    }
    
    public static String extractReporter (WebDriver driver) {
        return extractFromIssueTab(driver, 5, 2);
    }
    
    public static String extractAssigned (WebDriver driver) {
        return extractFromIssueTab(driver, 6, 2);
    }
    
    public static String extractPriority (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 2);
    }
    
    public static String extractSeverity (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 4);
    }
    
    public static String extractReproducibility (WebDriver driver) {
        return extractFromIssueTab(driver, 7, 6);
    }
    
    public static String extractStatus (WebDriver driver) {
        return extractFromIssueTab(driver, 8, 2);
    }
    
    public static String extractResolution (WebDriver driver) {
        return extractFromIssueTab(driver, 8, 4);
    }
    
    public static String extractPlatform (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 2);
    }
    
    public static String extractOs (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 4);
    }
    
    public static String extractOsVersion (WebDriver driver) {
        return extractFromIssueTab(driver, 9, 6);
    }
    
    public static String extractSummary (WebDriver driver) {
        return extractFromIssueTab(driver, 11, 2);
    }
    
    public static String extractDescription (WebDriver driver) {
        return extractFromIssueTab(driver, 12, 2);
    }
    
    public static List<String> extractTags (WebDriver driver) {
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
    
    public static String extractStepsToReproduce (WebDriver driver) {
        try {
            String     xPath      = "//td[text()='Steps To Reproduce' and @class='category']";
            WebElement strTitle   = driver.findElement(By.xpath(xPath));
            WebElement strElement = strTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No steps to reproduce.");
            return null;
        }
    }
    
    public static String extractAdditionalInformation (WebDriver driver) {
        try {
            String     xPath      = "//td[text()='Additional Information' and @class='category']";
            WebElement aiTitle    = driver.findElement(By.xpath(xPath));
            WebElement strElement = aiTitle.findElement(By.xpath("./following-sibling::td"));
            return strElement.getText();
        } catch (NoSuchElementException e) {
            System.err.println(e.getClass().getSimpleName() + " : No additional information.");
            return null;
        }
    }
    
    public static List<String> extractAttachedFiles (WebDriver driver) throws NoSuchElementException {
        
        WebElement f1           = driver.findElement(By.id("attachments"));
        WebElement f2           = f1.findElement(By.xpath("./parent::td"));
        WebElement filesElement = f2.findElement(By.xpath("./following-sibling::td"));
        
        if (filesElement.getText().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<WebElement> filesElements = filesElement.findElements(By.xpath("//a[text()='^']"));
        
        return filesElements.stream().map((x) -> x.getAttribute("href")).toList();
    }
    
    public static String extractFile (WebDriver driver, int fileId) throws IssueFileNotFound {
        WebElement f1           = driver.findElement(By.id("attachments"));
        WebElement f2           = f1.findElement(By.xpath("./parent::td"));
        WebElement filesElement = f2.findElement(By.xpath("./following-sibling::td"));
        
        if (filesElement.getText().isEmpty()) {
            driver.quit();
            throw new IssueFileNotFound("Issue file with id " + fileId + " not found");
        }
        
        List<WebElement> fileLinks = filesElement.findElements(By.xpath("//a[text()='^']"));
        for(WebElement linkElement : fileLinks) {
            String hrefValue = linkElement.getAttribute("href");
            if (hrefValue.contains("id=" + fileId + "&")){
                driver.quit();
                return hrefValue;
            }
        }
        driver.quit();
        throw new IssueFileNotFound("Issue file with id " + fileId + " not found");
    }
    
    public static WebElement ExtractDeleteFileButton (WebDriver driver, int fileId) throws IssueFileNotFound {
        WebElement f1           = driver.findElement(By.id("attachments"));
        WebElement f2           = f1.findElement(By.xpath("./parent::td"));
        WebElement filesElement = f2.findElement(By.xpath("./following-sibling::td"));
        
        if (filesElement.getText().isEmpty()) {
            driver.quit();
            throw new IssueFileNotFound("Issue file with id " + fileId + " not found");
        }
        
        List<WebElement> fileLinks = filesElement.findElements(By.xpath("//a[text()='Delete']"));
        for(WebElement linkElement : fileLinks) {
            String hrefValue = linkElement.getAttribute("href");
            if (hrefValue.contains("id=" + fileId + "&")){
                return linkElement;
            }
        }
        driver.quit();
        throw new IssueFileNotFound("Issue file with id " + fileId + " not found");
    }
    
    private static LocalDateTime parseDate (String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
