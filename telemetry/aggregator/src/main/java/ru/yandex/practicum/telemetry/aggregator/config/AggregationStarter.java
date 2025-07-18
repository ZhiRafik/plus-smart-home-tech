package ru.yandex.practicum.telemetry.aggregator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.service.AggregatorService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter implements Runnable {

    private final AggregatorService aggregatorService;
    private final KafkaConsumer<String, SensorEventAvro> consumer =
            KafkaConsumerConfig.getSensorConsumer();

    private final Producer<String, SensorsSnapshotAvro> producer = KafkaProducerConfig.getSnapshotProducer();

    private volatile boolean running = true;

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of("telemetry.sensors.v1"));
            log.info("Aggregator запущен. Ожидаем события...");

            while (running) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();

                    Optional<SensorsSnapshotAvro> maybeSnapshot = aggregatorService.updateState(event);

                    maybeSnapshot.ifPresent(snapshot -> {
                        ProducerRecord<String, SensorsSnapshotAvro> outRecord = new ProducerRecord<>(
                                "telemetry.snapshots.v1",
                                snapshot.getHubId().toString(),
                                snapshot
                        );
                        producer.send(outRecord, (metadata, ex) -> {
                            if (ex != null) {
                                log.error("Ошибка при отправке снапшота в Kafka", ex);
                            }
                        });
                    });
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Завершаем: flush и commit");
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем продюсер и консьюмер");
                producer.close();
                consumer.close();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Получен сигнал завершения");
        running = false;
        consumer.wakeup(); // чтобы немедленно выйти из poll()
    }
}
