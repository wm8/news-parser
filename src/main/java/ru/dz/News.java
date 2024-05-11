package ru.dz;

public class News {
    public final String url;
    public final String title;
    public final String message;
    public final Long time;
    public final String author;
    public News(String url, String title, String message, Long time, String author) {
        this.url = url;
        this.title = title;
        this.message = message;
        this.time = time;
        this.author = author;
    }
}
