package com.oauth2.simple;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;

public class CustomResponseDefTrans extends ResponseDefinitionTransformer {
    public static String NONCE="";
    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource fileSource, Parameters parameters) {


        if(request.queryParameter("nonce").isPresent())
        {

            NONCE=request.queryParameter("nonce").firstValue();
            System.out.println("NONCE: "+NONCE);
        }
        if(request.getAbsoluteUrl().contains("/oauth/token"))
        {
            try {
                return okJson("{\"token_type\": \"Bearer\",\"access_token\":\""+SimpleApplication.accessToken("krishna@test.com")+"\",\"id_token\":\""+SimpleApplication.generateToken("krishna@test.com",NONCE)+"\"}").build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseDefinition;
    }

    @Override
    public String getName() {
        return "nonce-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
