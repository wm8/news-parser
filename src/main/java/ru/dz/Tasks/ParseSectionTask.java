package ru.dz.Tasks;

import ru.dz.Daemon;
import ru.dz.MyLogger;
import ru.dz.RiaParser;

import java.util.Set;

public class ParseSectionTask extends Task {
    ParseSectionTask() {}
    public ParseSectionTask(String htmlData) {
        this.html = htmlData;
        this.type = Type.PARSE_SECTION;
    }
    protected String html;

    public String getHtml() { return html; }

    @Override
    public void run() {
        Set<String> urls = RiaParser.getNewsUrlsFromSection(html);
        MyLogger.info("section parsed!");
        urls.forEach(x -> Daemon.addTask(new DownloadPageTask(x, Type.PARSE_NEWS)));
    }
}
