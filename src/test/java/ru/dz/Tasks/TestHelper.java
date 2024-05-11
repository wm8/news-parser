package ru.dz.Tasks;

import ru.dz.News;

import javax.enterprise.inject.New;

import static org.junit.Assert.assertEquals;

public class TestHelper {
    public static void assertNews(News n) {
        assertEquals("url", n.url);
        assertEquals("title", n.title);
        assertEquals("message", n.message);
        assertEquals("author", n.author);
        assertEquals(1L, (long) n.time);
    }
    public static News createNews() {
        return new News("url", "title", "message", 1L, "author");
    }
}
