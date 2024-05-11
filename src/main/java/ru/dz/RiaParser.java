package ru.dz;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RiaParser {
    public static Set<String> getSectionsList(String htmlText) {
        Document doc = Jsoup.parse(htmlText);
        Set<String> sections = doc.getElementsByClass("cell-extension__item m-with-title")
                .stream().map(x -> x.childNode(0).attr("href"))
                .filter(x -> !x.startsWith("http"))
                .collect(Collectors.toSet());
        return sections;
    }
    public static Set<String> getNewsUrlsFromSection(String htmlText) {
        Document doc = Jsoup.parse(htmlText);
        return doc.getElementsByClass("list-item__title color-font-hover-only")
                .stream().map(x -> x.attr("href"))
                .collect(Collectors.toSet());
    }

    public static List<News> loadNewsFromSection(String htmlText) {
        Document doc = Jsoup.parse(htmlText);
        List<News> result = new ArrayList<>();
        doc.getElementsByClass("list-item__title color-font-hover-only")
                .stream().map(x -> x.attr("href")).forEach(url -> {
                    Optional<String> htmlData = Utils.getHtmlString(url);
                    htmlData.ifPresent(x -> parseNews(x, url).ifPresent(result::add));
                });
        return result;
    }

    public static boolean checkIfNewsIsValid(Document doc, String url, String[] classes) {

        boolean isOk = true;
        for (String classType : classes) {
            if (doc.getElementsByClass(classType).isEmpty()) {
                //MyLogger.info("%s has no attr %s", url, classType);
                isOk = false;
            }
        }
        return isOk;
    }
    public static Long parseDate(Document doc) {
        String dateString = doc.getElementsByClass("article__info-date")
                .get(0).childNode(0).childNode(0).toString();
        Long timestamp = Utils.DateStringToLong(dateString);
        return timestamp;
    }

    public static Optional<News> parseNewsOfAnotherFormat(Document doc, String url) {
        String[] classes = {"white-longread__header-title", "white-longread__header-author", "article__info-date"};
        if (!checkIfNewsIsValid(doc, url, classes)) {
            MyLogger.info("Couldn't parse news: %s", url);
            return Optional.empty();
        }
        Long timestamp = parseDate(doc);
        String title = doc.getElementsByClass("white-longread__header-title").text();
        String author = doc.getElementsByClass("white-longread__header-author").text();
        String message = StringUtil.join(doc.getElementsByClass("white-longread__block white-longread__text")
                .stream().filter(Element::hasText)
                .map(Element::text)
                .collect(Collectors.toList()), "\n");
        return Optional.of(new News(url, title, message, timestamp, author));
    }

    public static Optional<News> parseNews(String htmlText, String url) {
        Document doc = Jsoup.parse(htmlText);
        String[] classes = {"article__info-date", "article__title", "article__block"};
        if (!checkIfNewsIsValid(doc, url, classes)) {
            return parseNewsOfAnotherFormat(doc, url);
        }
        Long timestamp = parseDate(doc);
        String title = doc.getElementsByClass("article__title").get(0).childNode(0).toString().strip();
        String message = StringUtil.join(doc.getElementsByClass("article__block")
                .stream().filter(Element::hasText)
                .map(Element::text)
                .collect(Collectors.toList()), "\n");
        return Optional.of(new News(url, title, message, timestamp, ""));
    }
}
