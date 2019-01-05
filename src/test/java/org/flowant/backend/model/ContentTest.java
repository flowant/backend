package org.flowant.backend.model;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ContentTest {
    static String title = "title";
    
    static String ingredients = "ingredients";
    static long prepareSeconds = 60;
    static long cookSeconds = 300;
    static int servings = 0;
    static int calory = 1000; //TODO calculate with servings
    static String nutritionFacts = "nutritionFacts";

    static String url = "url";

    static String sentences = "sentences";
    static String content = "content";

    static int rating = 10;
    static int liked = 200;
    static int reported = 1000;

    static String name = "name";

    public static Content small(int s) {
        return Content.of(UUID.randomUUID(), title + s, 
                Review.of(Reputation.of(rating + s, liked + s, reported + s)), CRUDZonedTime.now());
    }

    public static Content small() {
        return small(0);
    }

    public static Content large(int s) {
        return Content.builder().id(UUID.randomUUID()).title(title + s)
                .extend(new Recipe(List.of(ingredients + s), prepareSeconds + s, cookSeconds + s,
                        servings + s, calory + s, nutritionFacts + s))
                .fileRefs(List.of(new FileRef(url + s, UUID.randomUUID())))
                .sentences(sentences + s)
                .tags(List.of(Tag.of(name + s)))
                .review(new Review(Reputation.of(rating + s, liked + s, reported + s),
                        List.of(new Reply(content + s, Reputation.of(rating + s, liked + s, reported + s),
                                CRUDZonedTime.now()))))
                .crudTime(CRUDZonedTime.now())
                .build();
    }

    public static Content large() {
        return large(0);
    }

    @Test
    public void testMaker() {
        log.debug("Content:{}", small()::toString);
        log.debug("Content:{}", large()::toString);
    }
}
