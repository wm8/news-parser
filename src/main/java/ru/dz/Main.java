package ru.dz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import ru.dz.Tasks.DownloadPageTask;
import ru.dz.Tasks.Task;

import javax.enterprise.inject.New;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ru.dz.Utils.getHtmlString;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static final String BASE_URL = "https://ria.ru/";

    public static void main(String[] args) {

        Daemon daemon = new Daemon();
        Daemon.addTask(new DownloadPageTask(BASE_URL, Task.Type.PARSE_MAIN));
        daemon.runDaemon();


    }

}