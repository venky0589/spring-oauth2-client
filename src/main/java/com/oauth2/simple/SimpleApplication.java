package com.oauth2.simple;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootApplication

public class SimpleApplication
		//implements CommandLineRunner
{

	public static void main(String[] args) {
		SpringApplication.run(SimpleApplication.class, args);
	}
	WireMockServer wireMockServer=null;


	@EventListener
	public void handleContextRefreshedEvent(ContextStoppedEvent event)
	{
		WireMock.reset();
		wireMockServer.stop();



	}


	//@Override
	public void run(String... args) throws Exception {
		System.out.println("WIremock started");
		wireMockServer = new WireMockServer(wireMockConfig().port(8077).extensions(new ResponseTemplateTransformer(true))); //No-args constructor will start on port 8080, no HTTPS
		wireMockServer.start();
		wireMockServer.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/html")
						.withBodyFile("login.html")));

		wireMockServer.stubFor(post(urlPathEqualTo("/login"))
				.willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}{{{form.redirectUri}}}?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));

		wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
				.willReturn(okJson("{\"token_type\": \"Bearer\",\"access_token\":\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}")));

		wireMockServer.stubFor(get(urlPathEqualTo("/userinfo"))
				.willReturn(okJson("{\"sub\":\"my-id\",\"email\":\"bwatkins@test.com\",\"name\":\"bwatkins@test.com\"}")));
	}
}
