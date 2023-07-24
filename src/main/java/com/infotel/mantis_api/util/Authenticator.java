package com.infotel.mantis_api.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Authenticator {

    public static WebDriver login()  {
        WebDriver driver = new ChromeDriver();
        driver.get("http://localhost/mantisbt/login_page.php");
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