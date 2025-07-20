package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

@Slf4j
public class HubEventConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final KafkaConsumer<String, byte[]> HUB_CONSUMER =
            createHubConsumer("analyzer-hub-group", ByteArrayDeserializer.class.getName());

    private static KafkaConsumer<String, byte[]> createHubConsumer(
            String groupId, String valueDeserializerClass) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        log.info("Создание KafkaConsumer для хабов с конфигурацией:");
        config.forEach((key, value) -> log.info("{} = {}", key, value));

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config);
        log.info("Hub KafkaConsumer успешно создан и готов к работе.");

        return consumer;
    }

    public static KafkaConsumer<String, byte[]> getHubConsumer() {
        return HUB_CONSUMER;
    }
}
