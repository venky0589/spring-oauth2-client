package com.oauth2.simple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.fluentlenium.adapter.junit.jupiter.FluentTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,

        classes=SimpleApplication.class)
@AutoConfigureMockMvc
//@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
//@AutoConfigureWireMock(port = 0)
public class WireMockOAuth2Test extends FluentTest {
    static final String APP_BASE_URL = "http://localhost:8080";

    @Autowired
    private MockMvc mockMvc;
    private static WebDriver webDriver;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
    public static WireMockServer wireMockServer = null;
//    @Mock
//    public OAuth2AccessToken accessToken;

//    @MockBean
//    OAuth2AuthorizedClientService clientService;

    @Override
    public WebDriver newWebDriver() {

        webDriver = new ChromeDriver();
        return webDriver;
    }

        @BeforeAll
    public static void setUp() {
            System.out.println("WIremock started");
            wireMockServer = new WireMockServer(wireMockConfig().port(8077)
                    .extensions(new ResponseTemplateTransformer(true),new CustomResponseDefTrans())); //No-args constructor will start on port 8080, no HTTPS
            wireMockServer.start();
            wireMockServer.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "text/html")
                            .withBodyFile("login.html")
                            .withTransformers("nonce-transformer")
                    ));

            wireMockServer.stubFor(post(urlPathEqualTo("/login"))
                    .willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}{{{form.redirectUri}}}?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));

            wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
                    .willReturn(
//						.withStatus(200)
//						.withHeader("Content-Type", "application/json")
//						.withBody("{\"token_type\": \"Bearer\",\"access_token\":\""+generateToken("test")+"\",\"id_token\":\""+generateToken("test")+"\"}")
                            okJson("{}")
                                    .withTransformers("nonce-transformer")
                            //okJson("{\"token_type\": \"Bearer\",\"access_token\":\""+generateToken("test", "test")+"\",\"id_token\":\""+generateToken("test", "test")+"\"}")
                    ));
            wireMockServer.stubFor(get(urlPathEqualTo("/.well-known/jwks.json"))
                    .willReturn(okJson("{\n" +
                            "\"keys\": [{\n" +
                            "    \"kty\": \"RSA\",\n" +
                            "    \"e\": \"AQAB\",\n" +
                            "    \"use\": \"sig\",\n" +
                            "    \"kid\": \"kPpF21pmMFChqnl75abKxMN_ePADtCW-ofMr5IpS5pA\",\n" +
                            "    \"alg\": \"RS256\",\n" +
                            "    \"n\": \"mZqUNf5URcBJoBRlu5TmainpOCWK8monMQ5JyuUho-RkVWrO5hyM30PVj5ThN8x43VP1QCKYhMAdV6YftcAKRfkrQFlgK5bHfMHY4rneUqd38E5bVhOy1okizkzzZm41JJEsjHnI05Qg4lPoirCwGJ-IMk5LdmztfquSnSDYfEmA6NsFVnzo0FZcQXRzZnFbnwtBbCmJgeL6z3M8TJ4qlgKab6wl4ufX99IsedyrhHsD239TK1jjcgKZ-u-KahVdJ0sntMmFKm7rNMXnlrOKrvPTM81MhhXo06lW-Eo_LmWOVuKK_v4dMzuhWP4OkNE8wWa5DGFuYTtZePDjyB8SRw\"\n" +
                            "}]\n" +
                            "}\n")));
            //	.willReturn(okJson(mapper.writeValueAsString(getPublicKeyJkws("/tmp/test/certificatename.der")))));

            wireMockServer.stubFor(get(urlPathEqualTo("/userinfo"))
                    .willReturn(okJson("{\"sub\":\"krishna@test.com\",\"email\":\"bwatkins@test.com\",\"name\":\"bwatkins@test.com\"}")));



        }
//    @BeforeAll
//    public static void setUp() {
//        wireMockServer = new WireMockServer(wireMockConfig().port(8077).extensions(new ResponseTemplateTransformer(true))); //No-args constructor will start on port 8080, no HTTPS
//        wireMockServer.start();
//
//        wireMockServer.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "text/html")
//                        .withBodyFile("login.html")
//                        .withTransformers("response-template"))
//        );
//        wireMockServer.stubFor(get(urlPathMatching("/favicon.ico"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "image/x-icon")
//                        .withBodyFile("favicon.ico")
//                        .withTransformers("response-template"))
//        );
//
//        wireMockServer.stubFor(post(urlPathEqualTo("/login"))
//                .willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}{{{form.redirectUri}}}?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));
//
//        wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
//                .willReturn(okJson("{\"token_type\": \"Bearer\",\"access_token\":\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}")));
//
//        wireMockServer.stubFor(get(urlPathEqualTo("/userinfo"))
//                .willReturn(okJson("{\"sub\":\"my-id\",\"email\":\"bwatkins@test.com\",\"name\":\"krishna@test.org\"}")));
        // More stubs later ...
//    }
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
     //   OidcUserInfo userInfo=OidcUserInfo.builder().name("TestName").build();
      //  OidcIdToken idToken = new OidcIdToken(generateToken("krishna"), Instant.now(),
      //          Instant.now().plusSeconds(3600), Collections.singletonMap(IdTokenClaimNames.SUB, "krishna"));
      //  DefaultOidcUser user = new DefaultOidcUser(AuthorityUtils.createAuthorityList("ROLE_USER"), idToken,userInfo);
        //OAuth2User user=OAuth2Utils.createOAuth2User("venky","test@test.org","ROLE_USER");
      //  Authentication principal=OAuth2Utils.getOAuthAuthenticationFor(user);//buildPrincipal();


        //ClientRegistration registration = this.clientRegistrationRepository.findByRegistrationId("wiremock");
       // OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(registration,"TestName",accessToken);
        //doReturn(client).when(clientService).loadAuthorizedClient(anyString(),anyString());
       // clientService.saveAuthorizedClient(client,principal);


        //webDriver.get(APP_BASE_URL+"/user");
        webDriver.get(APP_BASE_URL+"/ocuser");
        //webDriver.get(APP_BASE_URL + "/oauth2/authorization/wiremock");
        webDriver.findElement(By.linkText("wiremock")).sendKeys(Keys.ENTER);
        assertThat(webDriver.getCurrentUrl()).startsWith("http://localhost:8077/oauth/authorize");

        webDriver.findElement(By.name("username")).sendKeys("krishna@test.org");
        webDriver.findElement(By.name("password")).sendKeys("pass123");
        webDriver.findElement(By.id("submit")).click();
        //webDriver.get(APP_BASE_URL+"/user");
        assertThat(webDriver.getCurrentUrl()).contains("/ocuser");
        String content = webDriver.findElement(By.tagName("pre")).getText();
        ObjectMapper mapper=new ObjectMapper();
        JsonNode node=mapper.readTree(content);


        assertThat(node.get("name").asText()).isEqualTo("krishna@test.com");
    }

    @AfterAll
    public static void reset() {
       // WireMock.reset();
        //wireMockServer.stop();
    }

    public static String generateToken(String user)
    {
        String secretkey="qwertypassword";
        byte[] decodedSecret = Base64.getDecoder().decode(secretkey);

        //The JWT signature algorithm we will be using to sign the token
        String jwtToken = Jwts.builder()
                .setSubject(user)
                .setAudience("Test")
                .signWith(SignatureAlgorithm.HS256,decodedSecret).compact();
        return jwtToken;
    }

    static {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver"); // This is the OSX driver. You'll need to tweak this if you want to run on Windows or Linux.
    }

}
