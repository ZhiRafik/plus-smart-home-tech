package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.aggregator.config.serialization.SensorEventDeserializer;

import java.util.Properties;

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
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);
        return new KafkaConsumer<>(config);
    }

    public static KafkaConsumer<String, SensorEventAvro> getSensorConsumer() {
        return SENSOR_CONSUMER;
    }
}
