package ru.yandex.practicum.telemetry.aggregator.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.serialization.GeneralAvroSerializer;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    private static final Producer<String, SpecificRecordBase> PRODUCER = createProducer();
    private static final Producer<String, SensorsSnapshotAvro> SNAPSHOT_AVRO_PRODUCER = createSnapshotProducer();

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static Producer<String, SpecificRecordBase> getProducer() {
        return PRODUCER;
    }

    public static Producer<String, SensorsSnapshotAvro> getSnapshotProducer() {
        return SNAPSHOT_AVRO_PRODUCER;
    }

    public static Producer<String, SpecificRecordBase> createProducer() {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        log.info("Создание KafkaProducer (Generic Avro) с конфигурацией:");
        config.forEach((key, value) -> log.info("{} = {}", key, value));

        Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(config);
        log.info("KafkaProducer<SpecificRecordBase> успешно создан.");
        return producer;
    }

    public static Producer<String, SensorsSnapshotAvro> createSnapshotProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        log.info("Создание KafkaProducer (Snapshot) с конфигурацией:");
        config.forEach((key, value) -> log.info("{} = {}", key, value));

        Producer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(config);
        log.info("KafkaProducer<SensorsSnapshotAvro> успешно создан.");
        return producer;
    }
}
