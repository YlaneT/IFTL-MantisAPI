package com.infotel;


import org.openqa.selenium.WebDriver;

public class Main {


    public static void main (String[] args) throws InterruptedException {
        Authenticator auth = new Authenticator();
        WebDriver driver = auth.login();
        ViewIssues.getAllIssues(driver);

        driver.quit();
    }
}