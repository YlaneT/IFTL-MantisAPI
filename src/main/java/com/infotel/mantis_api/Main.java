package com.infotel.mantis_api;

import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main (String[] args) {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "ChromeDriverLogs.log");
        SpringApplication.run(Main.class, args);
    }
}