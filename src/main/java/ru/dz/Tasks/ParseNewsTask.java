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
        try {
            Optional<News> news = RiaParser.parseNews(this.html, this.url);
            news.ifPresentOrElse(value -> {
                MyLogger.info("%s parsed successfully", value.url);
                if (Daemon.getElasticSearchManager().indexNews(value)) {
                    MyLogger.info("News %s successfully saved", value.url);
                } else {
                    MyLogger.err("Error while saving %s", value.url);
                }
            }, () -> Daemon.getElasticSearchManager().deleteNews(url));
        } catch (Exception ex) {
            MyLogger.logException(ex);
        }
    }
}
