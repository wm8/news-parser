package ru.dz.Tasks;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest {
    private TaskManager manager;

    @Before
    public void init() {
        manager = new TaskManager();
    }
    @Test
    public void DownloadTaskSerializationTest() {
        DownloadPageTask task = new DownloadPageTask("url", Task.Type.PARSE_MAIN);
        String json = manager.taskToJsonString(task);
        Optional<Task> newTask = manager.jsonStringToTask(json);
        assertTrue(newTask.isPresent());
        assertTrue(newTask.get() instanceof DownloadPageTask);
        DownloadPageTask downloadPageTask = (DownloadPageTask) newTask.get();
        assertEquals("url", downloadPageTask.url);
        assertEquals(Task.Type.PARSE_MAIN, downloadPageTask.nextTaskType);
    }
    @Test
    public void ParseMainSerializationTest() {
        ParseMainTask task = new ParseMainTask("html", "url");
        String json = manager.taskToJsonString(task);
        Optional<Task> newTaskOpt = manager.jsonStringToTask(json);
        assertTrue(newTaskOpt.isPresent());
        assertTrue(newTaskOpt.get() instanceof ParseMainTask);
        ParseMainTask newTask = (ParseMainTask) newTaskOpt.get();
        assertEquals("url", newTask.baseUrl);
        assertEquals("html", newTask.html);
    }
    @Test
    public void ParseSectionsSerializationTest() {
        ParseSectionTask task = new ParseSectionTask("html");
        String json = manager.taskToJsonString(task);
        Optional<Task> newTaskOpt = manager.jsonStringToTask(json);
        assertTrue(newTaskOpt.isPresent());
        assertTrue(newTaskOpt.get() instanceof ParseSectionTask);
        ParseSectionTask newTask = (ParseSectionTask) newTaskOpt.get();
        assertEquals("html", newTask.html);
    }
    @Test
    public void ParseNewsSerializationTest() {
        ParseNewsTask task = new ParseNewsTask("html", "url");
        String json = manager.taskToJsonString(task);
        Optional<Task> newTaskOpt = manager.jsonStringToTask(json);
        assertTrue(newTaskOpt.isPresent());
        assertTrue(newTaskOpt.get() instanceof ParseNewsTask);
        ParseNewsTask newTask = (ParseNewsTask) newTaskOpt.get();
        assertEquals("url", newTask.url);
        assertEquals("html", newTask.html);
    }

}