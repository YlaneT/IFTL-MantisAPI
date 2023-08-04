package com.infotel.mantis_api.util.edit;

import com.infotel.mantis_api.exception.AccessDenied;
import com.infotel.mantis_api.exception.FieldNotFoundException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class EditIssue {
    
    /**
     * The button to start editing an issue.
     * <p>
     * After clicking it, page is updated, may further cause StaleElementException
     *
     * @param driver WebDriver of the page.
     * @return button to be clicked
     */
    public static WebElement extractEditButton (WebDriver driver) {
        WebElement editTable = driver.findElement(By.xpath("//table[3]//table"));
        String     xPath     = "//input[@value='Edit']";
        return editTable.findElement(By.xpath(xPath));
    }
    
    /**
     * The button to validate changes on edited issue.
     * <p>
     * Only appears after clicking extractEditButton()
     *
     * @param driver WebDriver of the page.
     * @return button to be clicked
     */
    public static WebElement extractUpdateButton (WebDriver driver) {
        String xPath = "//input[@value='Update Information']";
        return driver.findElement(By.xpath(xPath));
    }
    
    public static void editCategory (WebDriver driver, String category) throws FieldNotFoundException {
        Select categorySelect = new Select(driver.findElement(By.name("category_id")));
        try {
            categorySelect.selectByVisibleText(category);
        } catch (NoSuchElementException e1) {
            try {
                categorySelect.selectByValue(category);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Category " + category + " not found");
            }
        }
    }
    
    public static void editViewStatus (WebDriver driver, String viewStatus) throws FieldNotFoundException {
        Select categorySelect = new Select(driver.findElement(By.name("view_state")));
        try {
            categorySelect.selectByVisibleText(viewStatus);
        } catch (NoSuchElementException e1) {
            try {
                categorySelect.selectByValue(viewStatus);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("View status " + viewStatus + " doesn't exist");
            }
        }
    }
    
    public static void editReporter (WebDriver driver, String reporter) throws FieldNotFoundException {
        Wait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(2))
                                                       .pollingEvery(Duration.ofMillis(500))
                                                       .ignoring(NoSuchElementException.class);
        
        driver.findElement(By.id("reporter_id_edit")).click();
        WebElement reporterSelectElement = wait.until(d -> d.findElement(By.name("reporter_id")));
        Select reporterSelect = new Select(reporterSelectElement);
        try {
            reporterSelect.selectByVisibleText(reporter);
        } catch (NoSuchElementException e) {
            try {
                reporterSelect.selectByValue(reporter);
            } catch (NoSuchElementException ex) {
                throw new FieldNotFoundException("Reporter \"" + reporter + "\" not found");
            }
        }
    }
    
    public static void editAssigned (WebDriver driver, String assignee) throws FieldNotFoundException, AccessDenied {
        Select assigneeSelect = null;
        try {
            assigneeSelect = new Select(driver.findElement(By.name("handler_id")));
        } catch (NoSuchElementException e) {
            throw new AccessDenied("User doesn't have permission to edit assignee.");
        }
        try {
            assigneeSelect.selectByVisibleText(assignee);
        } catch (NoSuchElementException e1) {
            try {
                assigneeSelect.selectByValue(assignee);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("User \"" + assignee + "\" not found as assignee");
            }
        }
    }
    
    public static void editPriority (WebDriver driver, String priority) throws FieldNotFoundException {
        Select prioritySelect = new Select(driver.findElement(By.name("priority")));
        try {
            prioritySelect.selectByVisibleText(priority);
        } catch (NoSuchElementException e1) {
            try {
                prioritySelect.selectByValue(priority);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Priority \"" + priority + "\" doesn't exist");
            }
        }
    }
    
    public static void editSeverity (WebDriver driver, String severity) throws FieldNotFoundException {
        Select severitySelect = new Select(driver.findElement(By.name("severity")));
        try {
            severitySelect.selectByVisibleText(severity);
        } catch (NoSuchElementException e1) {
            try {
                severitySelect.selectByValue(severity);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Severity \"" + severity + "\" doesn't exist");
            }
        }
    }
    
    public static void editReproducibility (WebDriver driver, String reproducibilty) throws FieldNotFoundException {
        Select reproducibiltySelect = new Select(driver.findElement(By.name("reproducibility")));
        try {
            reproducibiltySelect.selectByVisibleText(reproducibilty);
        } catch (NoSuchElementException e1) {
            try {
                reproducibiltySelect.selectByValue(reproducibilty);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Reproducibilty \"" + reproducibilty + "\" doesn't exist");
            }
        }
    }
    
    public static void editStatus (WebDriver driver, String status) throws FieldNotFoundException, AccessDenied {
        Select statusSelect = null;
        try {
            statusSelect = new Select(driver.findElement(By.name("status")));
        } catch (NoSuchElementException e) {
            throw new AccessDenied("User doesn't have permission to edit issue status.");
        }
        try {
            statusSelect.selectByVisibleText(status);
        } catch (NoSuchElementException e1) {
            try {
                statusSelect.selectByValue(status);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Status \"" + status + "\" doesn't exist");
            }
        }
    }
    
    public static void editResolution (WebDriver driver, String resolution) throws FieldNotFoundException {
        Select resolutionSelect = new Select(driver.findElement(By.name("resolution")));
        try {
            resolutionSelect.selectByVisibleText(resolution);
        } catch (NoSuchElementException e1) {
            try {
                resolutionSelect.selectByValue(resolution);
            } catch (NoSuchElementException e2) {
                throw new FieldNotFoundException("Resolution \"" + resolution + "\" doesn't exist");
            }
        }
    }
    
    public static void editPlatform (WebDriver driver, String platform) {
        driver.findElement(By.id("platform")).clear();
        driver.findElement(By.id("platform")).sendKeys(platform);
    }
    
    public static void editOs (WebDriver driver, String os) {
        driver.findElement(By.id("os")).clear();
        driver.findElement(By.id("os")).sendKeys(os);
    }
    
    public static void editOsVersion (WebDriver driver, String osBuild) {
        driver.findElement(By.id("os_build")).clear();
        driver.findElement(By.id("os_build")).sendKeys(osBuild);
    }
    
    public static void editSummary (WebDriver driver, String summary) {
        driver.findElement(By.name("summary")).clear();
        driver.findElement(By.name("summary")).sendKeys(summary);
    }
    
    public static void editDescription (WebDriver driver, String description) {
        driver.findElement(By.name("description")).clear();
        driver.findElement(By.name("description")).sendKeys(description);
    }
    
    public static void editStepsToReproduce (WebDriver driver, String steps) {
        driver.findElement(By.name("steps_to_reproduce")).clear();
        driver.findElement(By.name("steps_to_reproduce")).sendKeys(steps);
    }
    
    public static void editAdditionalInformation (WebDriver driver, String additionalInfo) {
        driver.findElement(By.name("additional_information")).clear();
        driver.findElement(By.name("additional_information")).sendKeys(additionalInfo);
    }
    
    public static void editCustomField (WebDriver driver, String key, String value) throws FieldNotFoundException {
        try {
            String     xPathParent           = "//td[text()='" + key + "' and @class='category']";
            WebElement customCategoryElement = driver.findElement(By.xpath(xPathParent));
            String     xPathSibling          = "./following-sibling::td/input";
            WebElement customInput           = customCategoryElement.findElement(By.xpath(xPathSibling));
            customInput.clear();
            customInput.sendKeys(value);
        } catch (NoSuchElementException e) {
            throw new FieldNotFoundException("Custom field \"" + key + "\" not found");
        }
    }
    
    public static String verifyInput (String content, int maxLength) {
        content = content.replaceAll("\n", " ");
        if (content.length() > maxLength) {
            content = content.substring(0, maxLength);
        }
        return content;
    }
}
