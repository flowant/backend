package org.flowant.website.rest;

import static org.flowant.website.rest.UserRest.UN;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.flowant.website.BackendApplication;
import org.flowant.website.model.User;
import org.flowant.website.repository.UserRepository;
import org.flowant.website.storage.FileStorage;
import org.flowant.website.util.IdMaker;
import org.flowant.website.util.test.UserMaker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import junitparams.JUnitParamsRunner;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Log4j2
@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes=BackendApplication.class)
public class UserRestTest extends RestWithRepositoryTest<User, UUID, UserRepository> {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Before
    public void before() {
        super.before();

        setTestParams(UserRest.PATH_USER, User.class, User::getIdentity,
                UserMaker::smallRandom, UserMaker::largeRandom,
                user -> user.setFirstname("newFirstname"));

        setDeleter(user -> repo.deleteByIdWithRelationship(user.getIdentity()).subscribe());
    }

    @Test
    public void testCrud() {
        super.testCrud();
    }

    @Test
    public void testDeleteWithFiles() {
        User user = UserMaker.smallRandom();
        FileRestTest.postFiles(3, webTestClient).consumeWith(body -> user.setFileRefs(body.getResponseBody()));

        repo.save(user).block();
        cleaner.registerToBeDeleted(user); // in case of fails

        webTestClient.delete()
                .uri(UserRest.PATH_USER + UserRest.PATH_SEG_ID, user.getIdentity())
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(r -> {
                    user.getFileRefs().forEach(fileRef -> Assert.assertFalse(FileStorage.exist(fileRef.getIdentity())));
                    StepVerifier.create(repo.findById(user.getIdentity())).expectNextCount(0).verifyComplete();
                });
    }

    @Test
    public void testGetByUsername() {
        User user = UserMaker.largeRandom();
        repo.save(user).block();
        cleaner.registerToBeDeleted(user);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.scheme(SCHEME).host(host).port(port)
                        .path(UserRest.PATH_USER)
                        .queryParam(UN, user.getUsername()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class).consumeWith(r -> {
                    assertEquals(user, r.getResponseBody());
                });
    }

    public void signup(User user, boolean expected) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.scheme(SCHEME).host(host).port(port)
                        .path(UserRest.PATH_USER + UserRest.PATH_SIGNUP)
                        .build())
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().value(r -> {
                    assertEquals(expected, r.intValue() == HttpStatus.OK.value());
                });
    }

    @Test
    public void testSignup() {
        User user = UserMaker.smallRandom();
        cleaner.registerToBeDeleted(user);
        signup(user, true);
        signup(user, false);
    }

    @Test
    public void testUserPasswordEncoding() {
        String encoded = passwordEncoder.encode(IdMaker.randomUUID().toString());
        log.trace("encoded:{}", encoded);
    }

}
