package com.dgut.common.easemob.comm;

import com.dgut.common.easemob.api.AuthTokenAPI;
import com.dgut.common.easemob.comm.wrapper.ResponseWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PUNK on 2017/1/22.
 */
public class TokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(TokenGenerator.class);

    private Long expiredAt = -1L;

    private ClientContext context;

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public TokenGenerator() {
    }

    public TokenGenerator(ClientContext context) {
        this.context = context;
    }

    public String request(Boolean force) {
        force = (null == force) ? Boolean.FALSE : force;

        if( isExpired() || force ) {
            // Request new token
            if( null == context || !context.isInitialized() ) {
                log.error(MessageTemplate.INVAILID_CONTEXT_MSG);
                throw new RuntimeException(MessageTemplate.INVAILID_CONTEXT_MSG);
            }

            AuthTokenAPI authService = (AuthTokenAPI) context.getAPIFactory().newInstance(EasemobRestAPIFactory.TOKEN_CLASS);
            String clientId = ClientContext.getInstance().getClientId();
            String clientSecret = ClientContext.getInstance().getClientSecret();
            ResponseWrapper response = (ResponseWrapper) authService.getAuthToken(clientId, clientSecret);

            if( null != response && 200 == response.getResponseStatus() && null != response.getResponseBody() ) {
                ObjectNode responseBody = (ObjectNode) response.getResponseBody();
                String newToken =  responseBody.get("access_token").asText();
                Long newTokenExpire = responseBody.get("expires_in").asLong() * 1000;

                log.debug("New token: " + newToken);
                log.debug("New token expires: " + newTokenExpire);

                this.accessToken = newToken;
                this.expiredAt = System.currentTimeMillis() + newTokenExpire;
                log.info(MessageTemplate.print(MessageTemplate.REFRESH_TOKEN_MSG, new String[]{accessToken, expiredAt.toString()}));
            }
            else {
                log.error(MessageTemplate.REFRESH_TOKEN_ERROR_MSG);
            }
        }

        return accessToken;
    }

    public Boolean isExpired() {
        return System.currentTimeMillis() > expiredAt;
    }

}
