package com.mercadolibre.sdk;

import org.asynchttpclient.Param;
import org.asynchttpclient.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MercadoLibreTest extends Assert {

    String apiUrl = "http://localhost:3000";

    @Test
    public void testGetAuthUrl() {
        assertEquals(
                "https://auth.mercadolibre.com.ar/authorization?response_type=code&client_id=123456&redirect_uri=http%3A%2F%2Fsomeurl.com",
                new MercadoLibre(123456l, "client secret")
                        .getAuthUrl("http://someurl.com", MercadoLibre.AuthUrls.MLA));
    }

    @Test(expected = AuthorizationFailure.class)
    public void testAuthorizationFailure() throws AuthorizationFailure {
        MercadoLibre.apiUrl = apiUrl;

        new MercadoLibre(123456l, "client secret").authorize("bad code",
                "http://someurl.com");
    }

    @Test
    public void testAuthorizationSuccess() throws AuthorizationFailure {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret");
        m.authorize("valid code with refresh token", "http://someurl.com");

        assertEquals("valid token", m.getAccessToken());
        assertEquals("valid refresh token", m.getRefreshToken());
    }

    @Test
    public void testGet() throws MeliException, IOException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "valid token");

        Response response = m.get("/sites");

        assertEquals(200, response.getStatusCode());
        assertFalse(response.getResponseBody().isEmpty());
    }

	/*@Test
    public void testGetWithRefreshToken() throws MeliException, IOException {
		MercadoLibre.apiUrl = apiUrl;
		MercadoLibre m = new MercadoLibre(123456l, "client secret", "expired token",
				"valid refresh token");

		FluentStringsMap params = new FluentStringsMap();
		params.add("access_token", m.getAccessToken());
		Response response = m.get("/users/me", params);

		assertEquals(200, response.getStatusCode());
		assertFalse(response.getResponseBody().isEmpty());
	}*/

    @Test
    public void testErrorHandling() throws IOException, MeliException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "invalid token");

        List<Param> params = new ArrayList<Param>();
        params.add(new Param("access_token", m.getAccessToken()));
        Response response = m.get("/users/me", params);
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void testUserAgent() throws IOException, MeliException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "invalid token");

        List<Param> params = new ArrayList<Param>();
        params.add(new Param("access_token", m.getAccessToken()));
        Response response = m.get("/echo/user_agent", params);
        assertEquals(200, response.getStatusCode());
    }

    public void testPost() throws MeliException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "valid token");

        List<Param> params = new ArrayList<Param>();
        params.add(new Param("access_token", m.getAccessToken()));
        Response r = m.post("/items", params, "{\"foo\":\"bar\"}");

        assertEquals(201, r.getStatusCode());
    }

	/*
	public void testPostWithRefreshToken() throws MeliException {
		MercadoLibre.apiUrl = apiUrl;
		MercadoLibre m = new MercadoLibre(123456l, "client secret", "expired token",
				"valid refresh token");

		FluentStringsMap params = new FluentStringsMap();
		params.add("access_token", m.getAccessToken());
		Response r = m.post("/items", params, "{\"foo\":\"bar\"}");

		assertEquals(201, r.getStatusCode());
	}
	*/

    public void testPut() throws MeliException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "valid token");

        List<Param> params = new ArrayList<Param>();
        params.add(new Param("access_token", m.getAccessToken()));
        Response r = m.put("/items/123", params, "{\"foo\":\"bar\"}");

        assertEquals(200, r.getStatusCode());
    }

	/*
	public void testPutWithRefreshToken() throws MeliException {
		MercadoLibre.apiUrl = apiUrl;
		MercadoLibre m = new MercadoLibre(123456l, "client secret", "expired token",
				"valid refresh token");

		FluentStringsMap params = new FluentStringsMap();
		params.add("access_token", m.getAccessToken());
		Response r = m.put("/items/123", params, "{\"foo\":\"bar\"}");

		assertEquals(200, r.getStatusCode());
	}
	*/

    public void testDelete() throws MeliException {
        MercadoLibre.apiUrl = apiUrl;
        MercadoLibre m = new MercadoLibre(123456l, "client secret", "valid token");

        List<Param> params = new ArrayList<Param>();
        params.add(new Param("access_token", m.getAccessToken()));
        Response r = m.delete("/items/123", params);

        assertEquals(200, r.getStatusCode());
    }

	/*
	public void testDeleteWithRefreshToken() throws MeliException {
		MercadoLibre.apiUrl = apiUrl;
		MercadoLibre m = new MercadoLibre(123456l, "client secret", "expired token",
				"valid refresh token");

		FluentStringsMap params = new FluentStringsMap();
		params.add("access_token", m.getAccessToken());
		Response r = m.delete("/items/123", params);

		assertEquals(200, r.getStatusCode());
	}
	*/
}
