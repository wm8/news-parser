package ru.dz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NewsContainer {

    private final ReadWriteLock lock;
    private final List<News> newsCollection;

    public NewsContainer() {
        lock = new ReentrantReadWriteLock();
        newsCollection = new ArrayList<>();
    }

    public void addNews(News news) {
        lock.writeLock().lock();
        newsCollection.add(news);
        lock.writeLock().unlock();
    }

    public List<News> getNewsCollection() {
        lock.readLock().lock();
        List<News> collection = Collections.unmodifiableList(this.newsCollection);
        lock.readLock().unlock();
        return collection;
    }

    public int getNewsCount() {
        lock.readLock().lock();
        int count = newsCollection.size();
        lock.readLock().unlock();
        return count;
    }
}
