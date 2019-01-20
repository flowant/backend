package org.flowant.common.model;

import org.flowant.common.util.test.ContentMaker;
import org.junit.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ContentTest {

    @Test
    public void testMaker() {
        log.debug("Content:{}", ContentMaker.small()::toString);
        log.debug("Content:{}", ContentMaker.large()::toString);
    }

}