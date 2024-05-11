package ru.dz.Tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.dz.Daemon;
import ru.dz.MyLogger;
import ru.dz.News;
import ru.dz.RiaParser;

import java.util.Optional;

public class ParseNewsTask extends Task {
    ParseNewsTask() {}
    public ParseNewsTask(String htmlData, String url) {
        this.html = htmlData;
        this.url = url;
        this.type = Type.PARSE_NEWS;
    }
    protected String html;
    protected String url;

    public String getHtml() { return  html; }
    public String getUrl() { return  url; }

    @Override
    public void run() {
        Optional<News> news = RiaParser.parseNews(this.html, this.url);
        news.ifPresent(value -> {
            MyLogger.info("%s parsed successfully", value.url);
            Daemon.getContainer().addNews(value);
            MyLogger.info("Parsed %d news", Daemon.getContainer().getNewsCount());
        });
    }
}
