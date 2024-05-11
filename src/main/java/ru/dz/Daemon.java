package ru.dz;

import ru.dz.Tasks.Task;
import ru.dz.Tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Daemon {
    private final ExecutorService executor;
    private final TaskManager taskManager;
    private final NewsContainer container;

    private boolean shutDownFlag;
    private int noTaskCount;

    private static final int TIMEOUT = 1;

    public Daemon() {
        executor = Executors.newWorkStealingPool();
        taskManager = new TaskManager();
        container = new NewsContainer();
        noTaskCount = 0;
        if (instance == null) {
            instance = this;
        }
    }
    private static Daemon instance;

    public static Daemon getInstance() {
        if (instance == null) {
            instance = new Daemon();
        }
        return instance;
    }

    public TaskManager getTaskManager() { return taskManager; }

    public static void addTask(Task task) {
        getInstance().noTaskCount = 0;
        var taskManager = Daemon.getInstance().getTaskManager();
        taskManager.addTask(task);
    }

    public static NewsContainer getContainer() {
        return Daemon.getInstance().container;
    }

    private boolean processingTask() {
        Optional<Task> task = this.taskManager.getTask();
        if (task.isEmpty()) {
            noTaskCount++;
            return false;
        }
        this.launchTask(task.get());
        return true;
    }

    public void runDaemon() {
        MyLogger.info("Running daemon...");
        hookShutDownSignal();
        do {
            if (!this.processingTask()) {
                this.sleep();
            }
        } while (!shutDownFlag && noTaskCount < 10);

        executor.shutdown();
        boolean allFinished;
        try {
            allFinished = executor.awaitTermination(1000L * 60L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!allFinished) {
            MyLogger.err("Not all tasks was finished");
        }
    }

    private void launchTask(Task newTask) {
        /*Future<?> future =*/ executor.submit(newTask::run);
//        lock.writeLock().lock();
//        futures.add(future);
//        lock.writeLock().unlock();
    }



    public void setShutDownFlag() {
        this.shutDownFlag = true;
    }

    private void hookShutDownSignal() {
        final Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(
                new Thread(this::setShutDownFlag)
        );
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(TIMEOUT);
        } catch (final InterruptedException e) {
            MyLogger.logException(e);
            this.setShutDownFlag();
        }
    }

}
