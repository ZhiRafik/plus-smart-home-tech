package ru.yandex.practicum.telemetry.aggregator.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.aggregator.config.serialization.SensorEventDeserializer;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConsumerConfig {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final KafkaConsumer<String, SensorEventAvro> SENSOR_CONSUMER =
            createSensorConsumer("sensor-group", SensorEventDeserializer.class.getName());

    private static KafkaConsumer<String, SensorEventAvro> createSensorConsumer(
            String groupId, String valueDeserializerClass) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        log.info("Создание KafkaConsumer с конфигурацией:");
        config.forEach((key, value) -> log.info("{} = {}", key, value));

        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(config);
        log.info("KafkaConsumer успешно создан и готов к работе.");

        return consumer;
    }

    public static KafkaConsumer<String, SensorEventAvro> getSensorConsumer() {
        return SENSOR_CONSUMER;
    }
}
