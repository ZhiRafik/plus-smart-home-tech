package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.yandex.practicum.telemetry.serialization.GeneralAvroSerializer;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    private static final Producer<String, SpecificRecordBase> PRODUCER = createProducer();

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Bean
    public static Producer<String, SpecificRecordBase> createProducer() {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        return new KafkaProducer<>(config);
    }

    public static Producer<String, SpecificRecordBase> getProducer() {
        return PRODUCER;
    }

    public static Producer<String, SensorsSnapshotAvro> getSnapshotProducer() {
        return (Producer<String, SensorsSnapshotAvro>) (Producer<?, ?>) PRODUCER;
    }
}
