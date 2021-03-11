package com.oauth2.simple;
import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.configuration.FluentConfiguration;
import org.fluentlenium.core.annotation.Page;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.Capabilities;
                                        import org.openqa.selenium.WebDriver;
import java.util.concurrent.TimeUnit;   import org.openqa.selenium.chrome.ChromeDriver; 

import static org.assertj.core.api.Assertions.assertThat;

@Wait
@FluentConfiguration(webDriver = "chrome")
public class DuckDuckGoTest extends FluentTest {

    public DuckDuckGoTest() {
        super();
        System.setProperty("webdriver.chrome.driver","/usr/bin/chromedriver");
    }
    @Override
    public WebDriver newWebDriver() {

        return new ChromeDriver();
    }

    /*@Test
    public void titleOfDuckDuckGoShouldContainSearchQueryName() {
               goTo("https://duckduckgo.com");
        $("#search_form_input_homepage").fill().with("FluentLenium");
        $("#search_button_homepage").submit();
        assertThat(window().title()).contains("FluentLenium");
    }*/
    @Page
    DuckDuckMainPage duckDuckMainPage;

    @Test
    public void titleOfDuckDuckGoShouldContainSearchQueryName() {
        String searchPhrase = "searchPhrase";

        goTo(duckDuckMainPage)
                .typeSearchPhraseIn(searchPhrase)
                .submitSearchForm()
                .assertIsPhrasePresentInTheResults(searchPhrase);
    }
}