package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioService;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

    private final Consumer<String, byte[]> consumer;
    private final ScenarioService scenarioService;

    public SnapshotProcessor(ConsumerFactory<String, byte[]> consumerFactory,
                             ScenarioService scenarioService) {
        this.consumer = consumerFactory.createConsumer();
        this.scenarioService = scenarioService;
        this.consumer.subscribe(List.of("telemetry.snapshots.v1"));
    }

    @Override
    public void run() {
        log.info("Запуск SnapshotProcessor...");
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records) {
                try {
                    SensorEventAvro snapshot = deserialize(record.value());
                    scenarioService.processSnapshot(snapshot);
                } catch (Exception e) {
                    log.error("Ошибка при обработке снапшота", e);
                }
            }
            consumer.commitSync();
        }
    }

    private SensorEventAvro deserialize(byte[] data) throws IOException {
        DatumReader<SensorEventAvro> reader = new SpecificDatumReader<>(SensorEventAvro.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Завершение SnapshotProcessor...");
        consumer.close();
    }
}

