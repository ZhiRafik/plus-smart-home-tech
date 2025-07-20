package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AvroUtils;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioService;

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
        try {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        log.trace("Обрабатываю snapshot");
                        SensorsSnapshotAvro snapshot = AvroUtils.deserialize(record.value(), SensorsSnapshotAvro.class);
                        scenarioService.processSnapshot(snapshot);
                    } catch (Exception e) {
                        log.error("Ошибка при обработке снапшота", e);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал wakeup, завершаем SnapshotProcessor...");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в SnapshotProcessor", e);
        } finally {
            consumer.close();
            log.info("Kafka consumer закрыт");
        }
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
    }
}

