package ru.dz.Tasks;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.junit.Test;
import ru.dz.ElasticSearchManager;
import ru.dz.News;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ElasticSearchTest {
    private final String testServerUrl = "http://127.0.0.1:9200";
    private final String testIndexName = "test_news";

    @Test
    public void deleteIndex() throws IOException {
        ElasticSearchManager manager = new ElasticSearchManager(testServerUrl, testIndexName);
        manager.Init();
        manager.deleteIndex();
    }

    @Test
    public void firstTest() {
        ElasticSearchManager manager = new ElasticSearchManager(testServerUrl, testIndexName);
        manager.Init();
        assertTrue(manager.createIndex());
        News news = TestHelper.createNews();
        assertTrue(manager.indexNews(news));
        Optional<News> newsOpt = manager.getDocumentByUrl(news.url);
        assertTrue(newsOpt.isPresent());
        TestHelper.assertNews(newsOpt.get());

    }

    @Test
    public void allNewsTest() {
        ElasticSearchManager manager = new ElasticSearchManager(testServerUrl, testIndexName);
        manager.Init();
        assertTrue(manager.createIndex());
        List<News> newsCollection = manager.getNewsCollection(1000);
        assertFalse(newsCollection.isEmpty());

    }

    @Test
    public void MultiGetTest() throws IOException {
        ElasticSearchManager manager = new ElasticSearchManager(testServerUrl, testIndexName);
        manager.Init();
        manager.indexNews(new News("some text"));
        manager.indexNews(new News("some text 1"));
        manager.indexNews(new News("text 1"));
        var esClient = manager.getClient();
        SearchResponse<News> response = esClient.search(s -> s
                        .index(testIndexName)
                        .query(q -> q.match(v -> v.field("url").query("*text"))),
                News.class
        );
        List<News> newsColl = response.hits().hits()
                    .stream().map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        manager.deleteIndex();
    }
}
