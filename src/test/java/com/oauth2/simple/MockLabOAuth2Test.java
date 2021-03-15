package com.oauth2.simple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.fluentlenium.adapter.junit.FluentTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,

        classes=SimpleApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MockLabOAuth2Test extends FluentTest {
    static final String APP_BASE_URL = "http://localhost:9000";

    @Autowired
    private MockMvc mockMvc;
    private WebDriver webDriver;

    @Override
    public WebDriver newWebDriver() {

        webDriver= new ChromeDriver();
       return webDriver;
    }

    @Rule
    public WireMockRule mockOAuth2Provider = new WireMockRule(wireMockConfig()
            .port(8077)
            .extensions(new ResponseTemplateTransformer(true)));

    @Test
    public void logs_in_via_wiremock_sso() throws Exception {
        //webDriver.get(APP_BASE_URL+"/user");
        webDriver.get(APP_BASE_URL+"/api/user");
        //webDriver.get(APP_BASE_URL + "/oauth2/authorization/wiremock");
        webDriver.findElement(By.linkText("mocklab")).sendKeys(Keys.ENTER);
        assertThat(webDriver.getCurrentUrl()).startsWith("https://oauth.mocklab.io/oauth/authorize");

        webDriver.findElement(By.name("email")).sendKeys("krishna@test.org");
        webDriver.findElement(By.name("password")).sendKeys("pass123");
        webDriver.findElement(By.id("login-submit")).click();
        //webDriver.get(APP_BASE_URL+"/user");
        assertThat(webDriver.getCurrentUrl()).contains("/user");
        String content = webDriver.findElement(By.tagName("pre")).getText();
        ObjectMapper mapper=new ObjectMapper();
        JsonNode node=mapper.readTree(content);

        assertThat(node.get("email").asText()).isEqualTo("krishna@test.org");
    }

    @After
    public void reset() {
        if (webDriver != null) {
            webDriver.close();
        }
    }

    static {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver"); // This is the OSX driver. You'll need to tweak this if you want to run on Windows or Linux.
    }

}
