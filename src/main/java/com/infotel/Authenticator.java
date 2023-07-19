package com.infotel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Authenticator {

    public WebDriver login() throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        driver.get("http://localhost/mantisbt/login_page.php");
        Thread.sleep(1000);

        driver.findElement(By.name("username")).sendKeys("administrator");
        driver.findElement(By.name("password")).sendKeys("root");

        driver.findElement(By.className("button")).click();
        Thread.sleep(1000);
        return driver;
    }


}