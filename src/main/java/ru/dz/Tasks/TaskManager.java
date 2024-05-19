package ru.dz.Tasks;

//import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.GetResponse;
import ru.dz.Constants;
import ru.dz.Daemon;
import ru.dz.Main;
import ru.dz.MyLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class TaskManager {
    private final Map<Integer, Class> taskFactory = new HashMap<>() {{
        put(Task.Type.DOWNLOAD_PAGE.getCode(), DownloadPageTask.class);
        put(Task.Type.PARSE_MAIN.getCode(), ParseMainTask.class);
        put(Task.Type.PARSE_SECTION.getCode(), ParseSectionTask.class);
        put(Task.Type.PARSE_NEWS.getCode(), ParseNewsTask.class);
    }};


    public String taskToJsonString(Task task) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(task).toString();
    }

    public Optional<Task> jsonStringToTask(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(json);
            int taskType = node.get("type").asInt();
            if (!taskFactory.containsKey(taskType)) {
                MyLogger.err("Unknown task type: %d", taskType);
                return Optional.empty();
            }
            Task task = (Task) objectMapper.treeToValue(node, taskFactory.get(taskType));
            return Optional.of(task);
        } catch (Exception e) {
            MyLogger.logException(e);
            return Optional.empty();
        }
    }

    private ConnectionFactory createConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constants.RABBITMQ_HOST);
        factory.setPort(Constants.RABBITMQ_PORT);
        factory.setUsername(Constants.RABBITMQ_USER);
        factory.setPassword(Constants.RABBITMQ_PASS);
        return factory;
    }

    public Optional<Task> getTask() {
        var factory = this.createConnection();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.basicQos(1);
            boolean autoAck = false;
            GetResponse response = channel.basicGet(Constants.RABBITMQ_QUEUE_NAME, autoAck);
            if (response == null) {
                MyLogger.info("No tasks");
                return Optional.empty();
            } else {
                //AMQP.BasicProperties props = response.getProps();
                String message = new String(response.getBody(), "UTF-8");
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                return jsonStringToTask(message);
            }
        } catch (IOException | TimeoutException e) {
            MyLogger.err("Task receiving exception: %s", e.toString());
            return Optional.empty();
        }

    }

    public Optional<Task> getTaskAsync() {
        var factory = this.createConnection();
        try {
             try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
                 channel.queueDeclare(Constants.RABBITMQ_QUEUE_NAME, false, false, false, null);
                 MyLogger.info("Waiting for messages...");
                 AtomicReference<Optional<Task>> task = new AtomicReference<>(Optional.empty());
                 DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                     String message = new String(delivery.getBody(), "UTF-8");
                     task.set(jsonStringToTask(message));
                     if (task.get().isPresent()) {
                         MyLogger.info("Task received: %s", task.get().get().getType());
                     }
                     channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                 };
                 // Установка предварительного получения одного сообщения
                 channel.basicQos(1);
                 // Начать потребление сообщений из очереди
                 channel.basicConsume(Constants.RABBITMQ_QUEUE_NAME, false, deliverCallback, consumerTag -> {
                     MyLogger.info("Cancel");
                 });
                 Thread.sleep(1000);
                 return task.get();
             }
        } catch (IOException | TimeoutException | InterruptedException e) {
            MyLogger.err("Task receiving exception: %s", e.toString());
        }
        return Optional.empty();
    }

    public void addTask(Task task) {
        var factory = createConnection();
        try {
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(Constants.RABBITMQ_QUEUE_NAME, false, false, false, null);
                String message = taskToJsonString(task);
                channel.basicPublish("", Constants.RABBITMQ_QUEUE_NAME, null, message.getBytes());
                MyLogger.info("Sending task: %s", task.getType().toString());
            }
        } catch (IOException | TimeoutException e) {
            MyLogger.err("Task execution exception: %s", e.toString());
        }
    }

}
