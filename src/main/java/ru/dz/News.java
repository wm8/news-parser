package ru.dz;


public class News {
    public String url;
    public String title;
    public String message;
    public Long time;
    public String author;

    public News(String url, String title, String message, Long time, String author) {
        this.url = url;
        this.title = title;
        this.message = message;
        this.time = time;
        this.author = author;
    }
    public News(String url) {
        this.url = url;
        this.title = "";
        this.message = "";
        this.time = 0L;
        this.author = "";

    }
    private News() {

    }
}
