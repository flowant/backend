package org.flowant.authserver;

import static org.flowant.website.controller.JwkSetRest.JWK_SET_URI;
import static org.flowant.website.controller.UserInfoEndpointRest.ME;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.flowant.website.AuthserverApplication;
import org.flowant.website.OAuth2ServerConfig;
import org.flowant.website.model.User;
import org.flowant.website.repository.devutil.MockUserRepoUtil;
import org.flowant.website.util.test.UserMaker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.log4j.Log4j2;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                classes=AuthserverApplication.class)
@Log4j2
public class OAuth2JwtTokenIssuerTests {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    @Autowired
    WebApplicationContext wac;

    @Autowired
    FilterChainProxy springSecurityFilterChain;

    @Autowired
    OAuth2ServerConfig authConfig;

    @Autowired
    MockUserRepoUtil mockUserRepoUtil;

    private static MockMvc mockMvc;

    @Before
    public void setup() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                    .addFilter(springSecurityFilterChain)
                    .build();
        }
    }

    @Test
    public void testGetJWKs() throws Exception {
        ResultActions result = mockMvc.perform(get(JWK_SET_URI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        log.trace(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/oauth/token")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testClientCredentialsAccessToken() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");

        ResultActions result = mockMvc
                .perform(post("/oauth/token").params(params).with(httpBasic(
                        authConfig.getClientId(), authConfig.getClientSecret()))
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        log.trace(result.andReturn().getResponse().getContentAsString());
    }

    public String obtainPasswordAccessToken(String username, String password, String tokenName) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);

        ResultActions result = mockMvc
                .perform(post("/oauth/token").params(params).with(httpBasic(
                        authConfig.getClientId(), authConfig.getClientSecret()))
                        .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        String resultString = result.andReturn().getResponse().getContentAsString();
        log.trace(resultString);
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get(tokenName).toString();
    }

    @Test
    public void testPasswordAccessToken() throws Exception {
        User user = mockUserRepoUtil.saveUserWithEncodedPassword(UserMaker.smallRandom());
        obtainPasswordAccessToken(user.getUsername(), user.getPassword(), ACCESS_TOKEN);
        mockUserRepoUtil.deleteUser(user);
    }

    @Test
    public void testUserInfoUrlUnauthorized() throws Exception {
        mockMvc.perform(get(ME)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testUserInfoUrl() throws Exception {
        User user = mockUserRepoUtil.saveUserWithEncodedPassword(UserMaker.largeRandom());
        String accessToken = obtainPasswordAccessToken(user.getUsername(), user.getPassword(), ACCESS_TOKEN);

        MvcResult result = mockMvc.perform(get(ME).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()).andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains(user.getUsername()));
        log.trace(result);

        mockUserRepoUtil.deleteUser(user);
    }

    @Test
    public void testRefreshToken() throws Exception {
        User user = mockUserRepoUtil.saveUserWithEncodedPassword(UserMaker.largeRandom());
        String refreshToken = obtainPasswordAccessToken(user.getUsername(), user.getPassword(), REFRESH_TOKEN);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", REFRESH_TOKEN);
        params.add(REFRESH_TOKEN, refreshToken);

        ResultActions result = mockMvc
                .perform(post("/oauth/token").params(params).with(httpBasic(
                        authConfig.getClientId(), authConfig.getClientSecret()))
                        .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        String resultString = result.andReturn().getResponse().getContentAsString();
        resultString.contains(REFRESH_TOKEN);
        log.trace(resultString);

        mockUserRepoUtil.deleteUser(user);
    }
}
