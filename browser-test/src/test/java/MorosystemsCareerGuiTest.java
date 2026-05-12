import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MorosystemsCareerGuiTest {

    private RemoteWebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void openBrowser() throws Exception {

        /*
        ** Connecting to a port exposed by the selenium container.
        */
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        driver = new RemoteWebDriver(new URI("http://selenium:4444").toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {

        /*
        ** Save a screenshot of the browser after the test and do some cleanup.
        */
        try {
            if (driver != null) {
                String testMethodName = testInfo.getTestMethod().get().getName();
                try {
                    Path dir = Path.of("target", "screenshots");
                    Files.createDirectories(dir);
                    Path file = dir.resolve(testMethodName + ".png");
                    Files.write(file, driver.getScreenshotAs(OutputType.BYTES));
                    System.err.println("Saved screenshot: " + file.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Could not save screenshot: " + e.getMessage());
                }
            }
        } finally {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
            wait = null;
        }
    }

    @Test
    void testCareerCityFilter() {

        /*
        ** Open the career page.
        */
        driver.get("https://www.morosystems.cz/");

        /*
        ** Click the necessary only button to dismiss the cookies if it appears. Ignore if it doesn't.
        */
       try {
           WebElement necessaryOnly = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div #cookiescript_reject")));
           necessaryOnly.click();
       } catch (Exception ignore) {
       }

        /*
        ** Click the career link.
        */
        WebElement kariera = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.m-main__link[href*='kariera']")));
        kariera.click();

        /*
        ** Wait for the career roles to be loaded.
        */
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pozice")));

        /*
        ** We don't have BE access at the moment, so we need to scan the DOM to get the career roles.
        ** The preferred way would be to use some kind of BE API to cross-check the data.
        */

        /*
        ** First retrieve all the cities from the dropdown.
        */
        Set<String> cities = new LinkedHashSet<>();
        for (WebElement label : driver.findElements(By.cssSelector("#pozice label.inp-custom-select__item.js-filter__link"))) {
            String df = label.getDomAttribute("data-filter");
            if (df != null && !df.isBlank()) {
                String value = df.strip();
                cities.add(value);
            }
        }

        /*
        ** Then retrieve all the hrefs for each city.
        */
        Map<String, List<String>> scanned = new HashMap<>();
        for (String city : cities) {
            List<String> hrefs = new ArrayList<>();
            for (WebElement we : driver.findElements(By.cssSelector("#pozice li.c-positions__item[data-filter*='" + city + "']"))) {
                String href = we.findElement(By.cssSelector("a.c-positions__link")).getDomProperty("href").strip();
                hrefs.add(href);
            }
            scanned.put(city, hrefs);
        }

        /*
        ** Then iterate over each city and check if the visible roles match what we expect.
        */

        for (String city : cities) {

            /*
            ** Click the city dropdown.
            */
            WebElement citiesDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#pozice .inp-custom-select__select")));
            citiesDropdown.click();

            /*
            ** Click the city option and wait for the roles to load.
            */
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#pozice label.inp-custom-select__item.js-filter__link[data-filter='" + city + "']")));
            option.click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#pozice .inp-custom-select__select-wrap"), city));

            /*
            ** Get the visible roles.
            */
            Set<String> visible = driver.findElements(By.cssSelector("#pozice li.c-positions__item a.c-positions__link")).stream()
                    .filter(WebElement::isDisplayed)
                    .map(a -> a.getDomProperty("href"))
                    .collect(Collectors.toCollection(HashSet::new));
            Set<String> expected = new HashSet<>(scanned.get(city));

            /*
            ** Check that the visible roles match the expected roles.
            */
            assertEquals(
                    expected,
                    visible,
                    "Visible roles for filter '" + city + "' should match rows whose data-filter lists that city");
        }
    }
}
