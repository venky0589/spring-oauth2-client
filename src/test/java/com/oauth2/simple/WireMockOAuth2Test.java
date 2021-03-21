package com.oauth2.simple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.fluentlenium.adapter.junit.jupiter.FluentTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,

        classes=SimpleApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WireMockOAuth2Test extends FluentTest {
    static final String APP_BASE_URL = "http://localhost:9000";

    @Autowired
    private MockMvc mockMvc;
    private static WebDriver webDriver;

    @Override
    public WebDriver newWebDriver() {

        webDriver= new ChromeDriver();
       return webDriver;
    }

    public static WireMockServer wireMockServer=null;
    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8077).extensions(new ResponseTemplateTransformer(true))); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();

        wireMockServer.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBodyFile("login.html")
                        .withTransformers("response-template"))
        );
        wireMockServer.stubFor(get(urlPathMatching("/favicon.ico"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "image/x-icon")
                        .withBodyFile("favicon.ico")
                        .withTransformers("response-template"))
        );

        wireMockServer.stubFor(post(urlPathEqualTo("/login"))
                .willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}{{{form.redirectUri}}}?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));

        wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
                .willReturn(okJson("{\"token_type\": \"Bearer\",\"access_token\":\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}")));

        wireMockServer.stubFor(get(urlPathEqualTo("/userinfo"))
                .willReturn(okJson("{\"sub\":\"my-id\",\"email\":\"bwatkins@test.com\",\"name\":\"krishna@test.org\"}")));
        // More stubs later ...
    }
//    @Test
//    public void testShowAuthenticationInfo () {
//        goTo("http://localhost:8099/api/token");
//
//        fill("input[name='username']").with("bwatkins");
//        fill("input[name='password']").with("password");
//        find("input[type='submit']").click();
//
//        assertThat(pageSource()).contains("username\":\"bwatkins\"");
//        assertThat(pageSource()).contains("my-fun-token");
//    }

    @Test
    public void logs_in_via_wiremock_sso() throws Exception {
        //webDriver.get(APP_BASE_URL+"/user");
        webDriver.get(APP_BASE_URL+"/user");
        //webDriver.get(APP_BASE_URL + "/oauth2/authorization/wiremock");
        webDriver.findElement(By.linkText("wiremock")).sendKeys(Keys.ENTER);
        assertThat(webDriver.getCurrentUrl()).startsWith("http://localhost:8077/oauth/authorize");

        webDriver.findElement(By.name("username")).sendKeys("krishna@test.org");
        webDriver.findElement(By.name("password")).sendKeys("pass123");
        webDriver.findElement(By.id("submit")).click();
        //webDriver.get(APP_BASE_URL+"/user");
        assertThat(webDriver.getCurrentUrl()).contains("/user");
        String content = webDriver.findElement(By.tagName("pre")).getText();
        ObjectMapper mapper=new ObjectMapper();
        JsonNode node=mapper.readTree(content);

        assertThat(node.get("name").asText()).isEqualTo("krishna@test.org");
    }

    @AfterAll
    public static void reset() {
       // WireMock.reset();
        wireMockServer.stop();
    }

    static {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver"); // This is the OSX driver. You'll need to tweak this if you want to run on Windows or Linux.
    }

}
