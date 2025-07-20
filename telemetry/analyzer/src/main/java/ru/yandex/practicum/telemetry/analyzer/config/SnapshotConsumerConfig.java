package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

@Slf4j
public class SnapshotConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final KafkaConsumer<String, byte[]> SNAPSHOT_CONSUMER =
            createSnapshotConsumer("analyzer-snapshot-group", ByteArrayDeserializer.class.getName());

    private static KafkaConsumer<String, byte[]> createSnapshotConsumer(
            String groupId, String valueDeserializerClass) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        log.info("Создание KafkaConsumer для снапшотов с конфигурацией:");
        config.forEach((key, value) -> log.info("{} = {}", key, value));

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config);
        log.info("Snapshot KafkaConsumer успешно создан и готов к работе.");

        return consumer;
    }

    public static KafkaConsumer<String, byte[]> getSnapshotConsumer() {
        return SNAPSHOT_CONSUMER;
    }
}

