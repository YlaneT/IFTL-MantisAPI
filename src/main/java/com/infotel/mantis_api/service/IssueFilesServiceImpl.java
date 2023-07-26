package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.IssueNotFoundException;
import com.infotel.mantis_api.util.Authenticator;
import com.infotel.mantis_api.util.extract_from.IssueDetails;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class IssueFilesServiceImpl implements IssueFilesService {
    
    @Override
    public List<String> searchAllIssueFiles (int id) throws IssueNotFoundException {
        WebDriver driver = Authenticator.login();
        
        driver.get("http://localhost/mantisbt/view.php?id=" + id);
        try {
            return IssueDetails.extractAttachedFiles(driver);
        } catch (NoSuchElementException e) {
            driver.quit();
            throw new IssueNotFoundException("Issue " + id + " not found");
        }
    }
    
    @Override
    public String searchIssueFile (int issueId, int fileId) {
        // TODO: Unimplemented
        return null;
    }
}
