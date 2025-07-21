package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.kafka.deserializer.HubEventDeserializer;

import java.util.Properties;

@Slf4j
public class HubEventConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> kafkaConsumerHubEvent() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-hub-group");

        return new KafkaConsumer<>(props);
    }
}
