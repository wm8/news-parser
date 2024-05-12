package ru.dz;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ElasticSearchManager {
    private static final Logger logger = LogManager.getLogger(ElasticSearchManager.class);
    private ElasticsearchClient client;
    private final String INDEX_NAME;
    private final String serverUrl;
    private final String apiKey;

    public ElasticSearchManager(String serverUrl, String indexName) {
        this.serverUrl = serverUrl;
        this.INDEX_NAME = indexName;
        this.apiKey = "";

    }

    public void Init() {
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient esClient = new ElasticsearchClient(transport);
        this.client = esClient;
        createIndex();
    }

    public boolean checkIfIndexExists() throws IOException {
        BooleanResponse response = client.indices().exists(i -> i.index(INDEX_NAME));
        return response.value();
    }

    public boolean createIndex() {
        try {
            if (!checkIfIndexExists()) {

                client.indices().create(i -> i.index(INDEX_NAME)
                        .mappings(m -> m.properties("url", p -> p.keyword(d -> d))
                                .properties("title", p -> p.keyword(d -> d))
                                .properties("message", p -> p.keyword(d -> d))
                                .properties("author", p -> p.keyword(d -> d))
                                .properties("title", p -> p.keyword(d -> d))
                                .properties("time", p -> p.date(l -> l))
                        )
                );
                logger.info(String.format("index %s successfully created!", INDEX_NAME));
                MyLogger.info("ES index %s created", INDEX_NAME);
            } else {
                logger.warn(String.format("index %s already exists", INDEX_NAME));
            }
            return true;
        } catch (IOException e) {
            logger.error(String.format("Error while creating index: %s", e));
            return false;
        }
    }

    public boolean indexNews(News news) {
        try {
            IndexResponse response = client.index(i -> i
                    .index(INDEX_NAME)
                    .id(Utils.getHash(news.url))
                    .document(news)
            );

            if (response.shards().failures().isEmpty()) {
                logger.info(String.format("News %s indexed", news.url));
                return true;
            }
            logger.error(String.format("News %s was not indexed", news.url));
            return false;

        } catch (IOException e) {
            logger.error(String.format("Error while indexing %s: %s", news.url, e));
            return false;
        }
    }

    public void deleteIndex() {
        try {
            var response = client.indices().delete(i -> i.index(INDEX_NAME));
            if (response.acknowledged()) {
                logger.info(String.format("Index %s deleted successfully", INDEX_NAME));
            } else {
                logger.warn("Failed to delete index " + INDEX_NAME);
            }
        } catch (IOException e) {
            MyLogger.logException(e);
        }
    }

    private String constructFailureString(ShardStatistics shards) {
        StringBuilder builder = new StringBuilder();
        shards.failures().stream().map(x -> x.reason().reason())
                .forEach(x -> builder.append(x).append('\n'));
        return builder.toString();
    }

    public boolean deleteNews(String url) {
        try {
            var response = client.delete(d -> d.index(INDEX_NAME).id(Utils.getHash(url)));
            boolean deleted = response.shards().total().longValue() == response.shards().successful().longValue();
            if (!deleted) {
                logger.error("Failure while deleting: " + constructFailureString(response.shards()));
            }
            return deleted;

        } catch (IOException e) {
            logger.error(e);
            return false;
        }
    }

    public Optional<News> getDocumentByUrl(String url) {
        try {
            GetResponse<News> response = client.get(g -> g
                            .index(INDEX_NAME)
                            .id(Utils.getHash(url)),
                    News.class
            );
            if (response.found()) {
                logger.info(String.format("Document of %s found", url));
                return Optional.of(response.source());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.error(String.format("Error while receiving %s: %s", url, e));
            return Optional.empty();
        }
    }

    public List<News> getNewsCollection(int size) {
        try {
            SearchResponse<News> response = client.search(s -> s
                            .index(INDEX_NAME).size(size),
                    News.class
            );
            List<News> newsColl = response.hits().hits()
                    .stream().map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
            logger.info(String.format("Received %d news", newsColl.size()));
            return newsColl;
        } catch (IOException e) {
            logger.error(String.format("Error while receiving %d news: %s", size, e));
            return new ArrayList<>();
        }
    }

    //Only for tests
    @Deprecated
    public ElasticsearchClient getClient () { return client; }
}
