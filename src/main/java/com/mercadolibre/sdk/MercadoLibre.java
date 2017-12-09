package com.mercadolibre.sdk;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.asynchttpclient.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MercadoLibre {

    public static String apiUrl = "https://api.mercadolibre.com";
    private static AsyncHttpClient httpClient = null;
    private String accessToken;
    private String refreshToken;
    private Long clientId;
    private String clientSecret;
    private Long expiresIn;
    private String scope;
    private String userId;
    private String tokenType;

    {
        if (httpClient == null) {
            AsyncHttpClientConfig cf = new DefaultAsyncHttpClientConfig.Builder().setUserAgent("MELI-JAVA-SDK-0.0.4").setMaxConnections(10).build();
            httpClient = new DefaultAsyncHttpClient(cf);
        }
    }

    public MercadoLibre(Long clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public MercadoLibre(Long clientId, String clientSecret, String accessToken) {
        this.accessToken = accessToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public MercadoLibre(Long clientId, String clientSecret, String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public String getScope() {
        return this.scope;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public Response get(String path) throws MeliException {
        return get(path, new ArrayList<Param>());
    }

    private BoundRequestBuilder prepareGet(String path, List<Param> params) {
        return httpClient.prepareGet(apiUrl + path)
                .addHeader("Accept", "application/json")
                .setQueryParams(params);
    }

    private BoundRequestBuilder prepareDelete(String path,
                                              List<Param> params) {
        return httpClient.prepareDelete(apiUrl + path)
                .addHeader("Accept", "application/json")
                .setQueryParams(params);
    }

    private BoundRequestBuilder preparePost(String path, List<Param> params, String body) {
        return httpClient.preparePost(apiUrl + path)
                .addHeader("Accept", "application/json")
                .setQueryParams(params)
                .setHeader("Content-Type", "application/json").setBody(body);
    }

    private BoundRequestBuilder preparePut(String path, List<Param> params, String body) {
        return httpClient.preparePut(apiUrl + path)
                .addHeader("Accept", "application/json")
                .setQueryParams(params)
                .setHeader("Content-Type", "application/json").setBody(body);
    }

    private BoundRequestBuilder preparePost(String path, List<Param> params) {
        return httpClient.preparePost(apiUrl + path)
                .addHeader("Accept", "application/json")
                .setQueryParams(params);
    }

    public Response get(String path, List<Param> params) throws MeliException {

        BoundRequestBuilder r = prepareGet(path, params);

        Response response;
        try {
            response = r.execute().get();
        } catch (Exception e) {
            throw new MeliException(e);
        }

        return response;
    }

    public void refreshAccessToken() throws AuthorizationFailure {
        List<Param> params = new ArrayList<Param>();
        params.add(new Param("grant_type", "refresh_token"));
        params.add(new Param("client_id", String.valueOf(this.clientId)));
        params.add(new Param("client_secret", this.clientSecret));
        params.add(new Param("refresh_token", this.refreshToken));
        try {
            BoundRequestBuilder req = preparePost("/oauth/token", params);
            parseToken(req);
        } catch (AuthorizationFailure e1) {
            System.out.println(e1.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param callback: The callback URL. Must be the applications redirect URI
     * @param authUrl:  The authorization URL. Get from MercadoLibre.AuthUrls
     * @return the authorization URL
     */
    public String getAuthUrl(String callback, AuthUrls authUrl) {
        try {
            return authUrl.getValue() + "/authorization?response_type=code&client_id="
                    + clientId
                    + "&redirect_uri="
                    + URLEncoder.encode(callback, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return authUrl + "/authorization?response_type=code&client_id="
                    + clientId + "&redirect_uri=" + callback;
        }
    }

    public void authorize(String code, String redirectUri) throws AuthorizationFailure {
        List<Param> params = new ArrayList<Param>();

        params.add(new Param("grant_type", "authorization_code"));
        params.add(new Param("client_id", String.valueOf(clientId)));
        params.add(new Param("client_secret", clientSecret));
        params.add(new Param("code", code));
        params.add(new Param("redirect_uri", redirectUri));

        BoundRequestBuilder r = preparePost("/oauth/token", params);

        parseToken(r);
    }

    private void parseToken(BoundRequestBuilder r) throws AuthorizationFailure {
        Response response = null;
        String responseBody = "";
        try {
            response = r.execute().get();
            responseBody = response.getResponseBody();
        } catch (InterruptedException e) {
            throw new AuthorizationFailure(e);
        } catch (ExecutionException e) {
            throw new AuthorizationFailure(e);
        }

        JsonParser p = new JsonParser();
        JsonObject object;

        try {
            object = p.parse(responseBody).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new AuthorizationFailure(responseBody);
        }

        if (response.getStatusCode() == 200) {

            this.accessToken = object.get("access_token").getAsString();

            JsonElement jsonElement = object.get("refresh_token");
            this.refreshToken = jsonElement != null ? object.get(
                    "refresh_token").getAsString() : null;
            /** News **/
            JsonElement jsonElementExpires = object.get("expires_in");
            this.expiresIn = jsonElementExpires != null ? Long.parseLong(object.get(
                    "expires_in").getAsString()) : null;

            JsonElement jsonElementScope = object.get("scope");
            this.scope = jsonElementScope != null ? object.get(
                    "scope").getAsString() : null;

            JsonElement jsonElementUserID = object.get("user_id");
            this.userId = jsonElementUserID != null ? object.get(
                    "user_id").getAsString() : null;

            JsonElement jsonElementToken = object.get("token_type");
            this.tokenType = jsonElementToken != null ? object.get(
                    "token_type").getAsString() : null;

        } else {
            throw new AuthorizationFailure(object.get("message").getAsString());
        }

    }

    private boolean hasRefreshToken() {
        return this.refreshToken != null && !this.refreshToken.isEmpty();
    }

    public Response post(String path, List<Param> params, String body) throws MeliException {

        BoundRequestBuilder r = preparePost(path, params, body);

        Response response;
        try {
            response = r.execute().get();
        } catch (Exception e) {
            throw new MeliException(e);
        }


        return response;
    }

    public Response put(String path, List<Param> params, String body) throws MeliException {

        BoundRequestBuilder r = preparePut(path, params, body);

        Response response;
        try {
            response = r.execute().get();
        } catch (Exception e) {
            throw new MeliException(e);
        }

        return response;
    }

    public Response delete(String path, List<Param> params) throws MeliException {
        BoundRequestBuilder r = prepareDelete(path, params);

        Response response;
        try {
            response = r.execute().get();
        } catch (Exception e) {
            throw new MeliException(e);
        }

        return response;
    }

    public BoundRequestBuilder head(String path) {
        return null;
    }

    public BoundRequestBuilder options(String path) {
        return null;
    }

    /**
     * Available auth endpoints
     */
    public static enum AuthUrls {
        MLA("https://auth.mercadolibre.com.ar"), // Argentina
        MLB("https://auth.mercadolivre.com.br"), // Brasil
        MCO("https://auth.mercadolibre.com.co"), // Colombia
        MCR("https://auth.mercadolibre.com.cr"), // Costa Rica
        MEC("https://auth.mercadolibre.com.ec"), // Ecuador
        MLC("https://auth.mercadolibre.cl"), // Chile
        MLM("https://auth.mercadolibre.com.mx"), // Mexico
        MLU("https://auth.mercadolibre.com.uy"), // Uruguay
        MLV("https://auth.mercadolibre.com.ve"), // Venezuela
        MPA("https://auth.mercadolibre.com.pa"), // Panama
        MPE("https://auth.mercadolibre.com.pe"), // Peru
        MPT("https://auth.mercadolibre.com.pt"), // Portugal
        MRD("https://auth.mercadolibre.com.do"); // Dominicana

        private String value;

        private AuthUrls(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
