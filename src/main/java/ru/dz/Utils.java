package ru.dz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.dz.Tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Utils {
    public static Long DateStringToLong(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
        long epochSeconds = dateTime.toEpochSecond(ZoneOffset.UTC);

        Instant instant = Instant.ofEpochSecond(epochSeconds);

        long timestampMillis = instant.toEpochMilli();
        return timestampMillis;
    }

    public static Optional<String> getHtmlString(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                MyLogger.err("Error %d while parsing %s", statusCode, urlString);
                return Optional.empty();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            reader.close();
            return Optional.of(content.toString());
        } catch (IOException e) {
            MyLogger.logException(e);
            return Optional.empty();
        }
    }

}
