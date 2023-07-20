package com.infotel.mantis_api.service;

import com.infotel.mantis_api.model.Issue;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IssuesServiceImpl implements IssuesService {
    
    public static WebDriver login ()  {
        WebDriver driver = new ChromeDriver();
        driver.get("http://localhost/mantisbt/login_page.php");
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        driver.findElement(By.name("username")).sendKeys("administrator");
        driver.findElement(By.name("password")).sendKeys("root");
        driver.findElement(By.cssSelector("input[value='Login']")).click();
        return driver;
    }
    
    /* Issue particularities
     * Issue 1 : Status acknowledged
     * Issue 2 : Contains Steps to reproduce
     * Issue 3 : Contains Tags / Attached file
     * Issue 4 : Contains custom field ("custom field numero 1")
     * Issue 5 : Minimal & unassigned
     * Issue 6 : Contains Steps to reproduce / Additional information
     * Issue 7 : Maximal
     */
    
    @Override
    public Issue searchIssue (int id) {
        WebDriver driver = login();
        Issue      issue     = new Issue();
        
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
            
            for(int i = 0 ; i < links.size() ; i += 2) {
                issue.getTags().add(links.get(i).getText()); // Ajoute chaque tag à l'issue
            }
        }
        
        try {
            WebElement strTitle = driver.findElement(By.xpath("//td[text()='Steps To Reproduce']"));
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
    
    @Override
    public List<Issue> searchAllIssues (int pageSize, int page, List<String> select, int projectId, String filterId) {
        return null;
    }
    
    private static LocalDateTime parseDate (String date){
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
