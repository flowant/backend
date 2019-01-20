package org.flowant.common.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.flowant.common.model.User;
import org.flowant.common.model.UserTest;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest
public class UserRepositoryTest {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@Autowired
	UserRepository userRepository;

	Consumer<? super User> deleteUser = u -> userRepository.delete(u).subscribe();
	Consumer<? super Collection<User>> deleteUsers = l -> l.forEach(deleteUser);

	@Test
    @Parameters
    public void testSave(User user) {
        Flux<User> saveThenFind = userRepository.save(user).thenMany(userRepository.findById(user.getId()));
        StepVerifier.create(saveThenFind).consumeNextWith(deleteUser).verifyComplete();
    }
    public static List<User> parametersForTestSave() {
        return Arrays.asList(UserTest.small(), UserTest.large());
    }

    @Test
    public void testSaveAllFindAllById() {
        Flux<User> users = Flux.range(1, 5).map(UserTest::large).cache();
        Flux<User> saveAllThenFind = userRepository.saveAll(users)
                .thenMany(userRepository.findAllById(users.map(User::getId)));
        StepVerifier.create(saveAllThenFind).recordWith(ArrayList::new).expectNextCount(5)
        .consumeRecordedWith(deleteUsers).verifyComplete();
    }

}