package ru.dz.Tasks;

import ru.dz.Daemon;
import ru.dz.MyLogger;
import ru.dz.RiaParser;

import java.util.Set;

public class ParseMainTask extends Task {
    ParseMainTask() {}
    public ParseMainTask(String htmlData, String BASE_URL) {
        this.html = htmlData;
        this.type = Type.PARSE_MAIN;
        this.baseUrl = BASE_URL;
    }
    protected String html;
    protected String baseUrl;

    public String getHtml() { return html; }
    public String getBaseUrl() {return baseUrl; }

    @Override
    public void run() {
        Set<String> sections = RiaParser.getSectionsList(this.html);
        MyLogger.info("Main page of %s parsed", baseUrl);
        sections.stream().map(section -> baseUrl + section)
                .forEach(x -> Daemon.addTask(new DownloadPageTask(x, Type.PARSE_SECTION)));

    }
}
