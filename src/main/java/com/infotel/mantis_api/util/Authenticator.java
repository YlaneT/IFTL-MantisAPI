package com.infotel.mantis_api.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class Authenticator {
    
    @Value("${mantis.base-url}")
    private String baseUrl;
    
    public WebDriver login () {
        WebDriver driver = new ChromeDriver();
        
        driver.get(baseUrl + "/login_page.php");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        
        driver.findElement(By.name("username")).sendKeys("administrator");
        driver.findElement(By.name("password")).sendKeys("root");
        
        driver.findElement(By.className("button")).click();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            driver.quit();
            throw new RuntimeException(e);
        }
        
        return driver;
    }
}