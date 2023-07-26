package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.IssueFileNotFound;
import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.util.Authenticator;
import com.infotel.mantis_api.util.extract_from.IssueDetails;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class IssueFilesServiceImpl implements IssueFilesService {
    
    @Override
    public List<String> searchAllIssueFiles (int id) throws IssueNotFoundException {
        WebDriver driver = Authenticator.login();
        
        driver.get("http://localhost/mantisbt/view.php?id=" + id);
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
        WebDriver driver = Authenticator.login();
        List<String> issueFiles = new ArrayList<>();
        
        // Get all issue Files
        driver.get("http://localhost/mantisbt/view.php?id=" + issueId);
        try {
            return IssueDetails.extractFile(driver, fileId);
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new IssueNotFoundException("Issue " + issueId + " not found");
        }
    }
}
