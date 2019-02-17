package org.flowant.website.rest;

import java.util.UUID;
import java.util.function.Function;

import org.flowant.website.BackendApplication;
import org.flowant.website.model.Review;
import org.flowant.website.repository.BackendReviewRepository;
import org.flowant.website.util.test.ReviewMaker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes=BackendApplication.class)
public class ReviewRestTest extends BaseRestWithRepositoryTest<Review, UUID, BackendReviewRepository> {

    @Before
    public void before() {
        super.before();

        setTestParams(ReviewRest.REVIEW, Review.class, Review::getId,
                ReviewMaker::smallRandom, ReviewMaker::largeRandom,
                (Review r) -> {
                    r.setComment("newComment");
                    return r;
                });
    }

    @Test
    public void testCrud() {
        super.testCrud();
    }

    @Test
    public void testPagination() {
        Function<UUID, Review> supplier = (containerId) ->
                ReviewMaker.largeRandom().setContainerId(containerId);
        pagination(10, 1, supplier);
        pagination(10, 3, supplier);
        pagination(10, 5, supplier);
        pagination(10, 11, supplier);
    }

}
