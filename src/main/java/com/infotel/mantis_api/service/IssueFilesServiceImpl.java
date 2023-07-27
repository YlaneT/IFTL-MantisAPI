package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.util.Authenticator;
import com.infotel.mantis_api.util.extract_from.IssueDetails;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class IssueFilesServiceImpl implements IssueFilesService {
    
    @Autowired
    Authenticator auth;
    @Value("${mantis.base-url}")
    private String baseUrl;
    
    @Override
    public List<String> searchAllIssueFiles (int id) throws IssueNotFoundException {
        WebDriver driver = auth.login();
        
        driver.get(baseUrl + "/view.php?id=" + id);
        try {
            List<String> issues = IssueDetails.extractAttachedFiles(driver);
            driver.quit();
            return issues;
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new IssueNotFoundException("Issue " + id + " not found");
        }
    }
    
    @Override
    public String searchIssueFile (int issueId, int fileId) throws IssueNotFoundException, IssueFileNotFound {
        WebDriver driver = auth.login();
        
        driver.get(baseUrl + "/view.php?id=" + issueId);
        try {
            String fileUrl = IssueDetails.extractFile(driver, fileId);
            driver.quit();
            return fileUrl;
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new IssueNotFoundException("Issue " + issueId + " not found");
        }
    }
    
    @Override
    public void deleteIssueFile (int issueId, int fileId) throws IssueNotFoundException, IssueFileNotFound {
        WebDriver driver = auth.login();
        
        driver.get("http://localhost/mantisbt/view.php?id=" + issueId);
        try {
            IssueDetails.ExtractDeleteFileButton(driver, fileId).click();
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new IssueNotFoundException("Issue " + issueId + " not found");
        }
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        
        try {
            driver.findElement(By.xpath("//input[@value='Delete']")).click();
        } finally {
            driver.quit();
        }
    }
}
