package com.infotel.mantis_api;


import com.infotel.mantis_api.service.ViewIssues;
import com.infotel.mantis_api.util.Authenticator;
import org.openqa.selenium.WebDriver;

public class Main {


    public static void main (String[] args) throws InterruptedException {
        Authenticator auth = new Authenticator();
        WebDriver driver = auth.login();
        ViewIssues.getAllIssues(driver);

        driver.quit();
    }
}