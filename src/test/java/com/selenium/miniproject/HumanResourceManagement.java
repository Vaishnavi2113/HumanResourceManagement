package com.selenium.miniproject;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;


public class HumanResourceManagement{

    static WebDriver driver;
    static WebDriverWait wait;

    @Parameters({"Browser"})
    @BeforeClass(alwaysRun = true)
    public void launchBrowser(@Optional("Chrome") String Browser) {
        try {
            if (Browser.equalsIgnoreCase("Chrome")) {
                driver = new ChromeDriver();
            } else if (Browser.equalsIgnoreCase("FireFox") || Browser.equalsIgnoreCase("Firefox")) {
                driver = new FirefoxDriver();
            } else {
                System.out.println("Illegal Browser. Defaulting to Chrome.");
                driver = new ChromeDriver();
            }

            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to launch browser: " + e.getMessage());
            throw e;
        }
    }

    private void captureScreenshot(String fileName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(dtf);
            File dest = new File("./screenshots/" + fileName + "_" + timestamp + ".png");
            FileUtils.copyFile(src, dest);
            System.out.println("Screenshot saved: " + dest.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error saving screenshot: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error during screenshot capture: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void loginDetails() {
        driver.get("https://opensource-demo.orangehrmlive.com/");

        // Username
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        username.sendKeys("Admin");

        // Password
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        password.sendKeys("admin123");

        captureScreenshot("logIn page");

        // Login
        WebElement loginbutton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(@class,'orangehrm-login-button')]")));
        loginbutton.click();


        // Verify dashboard
        wait.until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = driver.getCurrentUrl();
        boolean hasDashboard = currentUrl.toLowerCase().contains("dashboard");
        System.out.println("Current URL after login: " + currentUrl);
        Assert.assertTrue(hasDashboard, "URL does not contain 'dashboard'. Actual URL: " + currentUrl);

    }

    @Test(priority = 2, dependsOnMethods = "loginDetails")
    public void goToAdmin() {
        // Admin tab
        WebElement adminTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(.,'Admin')]")));
        adminTab.click();

        captureScreenshot("Displays admin");

        // Job dropdown
        WebElement jobDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(.,'Job')]")));
        jobDropdown.click();

        // Verify Job Titles presence
        WebElement jobTitles = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id=\"app\"]//nav/ul/li[2]/ul/li[1]/a")));
        Assert.assertTrue(jobTitles.isDisplayed(), "'Job Titles' is NOT present in Job dropdown.");
        System.out.println("'Job Titles' is present in Job dropdown.");

        // Click Job Titles
        jobTitles.click();

    }

    @Test(priority = 3, dependsOnMethods = "goToAdmin")
    public void jobList() {

        WebDriverWait wait=new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement number = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(.,'Records')]")));
        String num = number.getText();
        String[] data = num.split("");
        String n = data[1]+data[2]; //29
        int len = Integer.parseInt(n);
        System.out.println("The list of job titles: ");

        for(int i=1; i<=len; i++) {
            WebElement job = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[1]/div[2]/div[2]/div/div/div[3]/div/div[2]/div["+i+"]/div/div[2]/div")));
            System.out.println(job.getText());
        }
    }

    @Test(priority = 4, dependsOnMethods = "goToAdmin")
    public void addJob() {
        // Add button
        WebElement addingJob = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'oxd-button--secondary')]")));
        addingJob.click();

        // Title
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("/html/body/div/div[1]/div[2]/div[2]/div/div/form/div[1]/div/div[2]/input")));
        title.sendKeys("Automation Tester");

        // Description
        WebElement description = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//textarea[@placeholder='Type description here']")));
        description.sendKeys("The role focuses on identifying defects early, improving test efficiency, and supporting reliable software releases through automated testing processes.");

        // Save
        WebElement savebutton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'orangehrm-left-space')]")));
        savebutton.click();

        captureScreenshot("adding_jobTitle_page");

    }

    @Test(priority = 5, dependsOnMethods = {"loginDetails"})
    public void logout() {
        // Open user dropdown
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(@class,'oxd-userdropdown')] | //span[@class='oxd-userdropdown-img']")));
        trigger.click();

        // Click Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Logout' or normalize-space()='Log Out' or contains(.,'Logout')]")));
        logout.click();

        // Verify logout by checking login page is shown
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assert.assertTrue(username.isDisplayed(), "Username field not visible after logout.");
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Logged out and closed the browser successfully.");
        }
    }

}