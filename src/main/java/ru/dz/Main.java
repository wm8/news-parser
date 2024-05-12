package ru.dz;

import ru.dz.Tasks.DownloadPageTask;
import ru.dz.Tasks.Task;

import static java.lang.System.exit;

public class Main {
    public static final String BASE_URL = "https://ria.ru/";

    public static void main(String[] args) {
        Constants.Init();
        Daemon daemon = new Daemon();
        Daemon.addTask(new DownloadPageTask(BASE_URL, Task.Type.PARSE_MAIN));
        daemon.runDaemon();

        var collection = Daemon.getElasticSearchManager().getNewsCollection(1000);
        MyLogger.info("News count in es: %d", collection.size());
        exit(0);
    }

}