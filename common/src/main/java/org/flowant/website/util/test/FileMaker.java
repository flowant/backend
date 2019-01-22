package org.flowant.website.util.test;

import java.util.UUID;

import org.flowant.website.model.CRUZonedTime;
import org.flowant.website.model.FileRef;
import org.junit.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileMaker {

    static UUID id;
    static String url = "url";
    static String contentType = "contentType";
    static String filename = "originalFilename";

    public static FileRef large(int s) {
        return FileRef.of(UUID.randomUUID(), url + s,
                contentType + s, filename + s, CRUZonedTime.now());
    }

    public static FileRef large() {
        return large(0);
    }

    @Test
    public void testMaker() {
        log.debug("Multimedia:{}", large()::toString);
    }

}