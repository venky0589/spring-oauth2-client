package com.oauth2.simple;

import org.fluentlenium.adapter.junit.FluentTest;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayTest extends FluentTest {
    public WebDriver driver;
    // Overrides the default driver
    @Override
    public WebDriver newWebDriver() {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        driver = new ChromeDriver();
        return driver;
    }

    @Test
    public void title_of_bing_should_contain_search_query_name() {
        goTo("http://www.bing.com");
        //fill("#sb_form_q").with("Teotti");
       // submit("#sb_form_go");
        //assertThat(window().title()).contains("Teotti");
    }
}