package ru.dz.Tasks;


import ru.dz.Daemon;
import ru.dz.MyLogger;
import ru.dz.Utils;
import java.util.Optional;


public class DownloadPageTask extends Task {
    DownloadPageTask() {

    }
    public DownloadPageTask(String url, Task.Type nextTaskType) {
        this.type = Type.DOWNLOAD_PAGE;
        this.url = url;
        this.nextTaskType = nextTaskType;
    }
    protected Type nextTaskType;
    protected String url;

    public Type getNextTaskType() { return nextTaskType; }
    public String getUrl() { return url; }

    @Override
    public void run() {
        Optional<String> data = Utils.getHtmlString(url);
        if (data.isEmpty()) {
            return;
        }
        String html = data.get();
        switch (nextTaskType) {
            case PARSE_MAIN -> Daemon.addTask(new ParseMainTask(html, url));
            case PARSE_SECTION -> Daemon.addTask(new ParseSectionTask(html));
            case PARSE_NEWS -> Daemon.addTask(new ParseNewsTask(html, url));
            default -> MyLogger.err("Unsupported next action %s after loading %s",
                    nextTaskType.toString(), url);
        }
    }
}
