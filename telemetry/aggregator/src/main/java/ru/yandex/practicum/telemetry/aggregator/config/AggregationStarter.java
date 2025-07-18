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
            log.info("Подписка на топик telemetry.sensors.v1");
            consumer.subscribe(List.of("telemetry.sensors.v1"));

            log.info("Aggregator запущен. Ожидаем события...");

            while (running) {
                log.trace("Начинаем poll Kafka...");
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));
                log.trace("Получено {} событий", records.count());

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.debug("Получено сообщение: offset={}, partition={}, key={}, timestamp={}, value={}",
                            record.offset(), record.partition(), record.key(), record.timestamp(), record.value());

                    SensorEventAvro event = record.value();
                    Optional<SensorsSnapshotAvro> maybeSnapshot = aggregatorService.updateState(event);

                    if (maybeSnapshot.isPresent()) {
                        SensorsSnapshotAvro snapshot = maybeSnapshot.get();
                        log.debug("Создан снапшот для hubId={}: {}", snapshot.getHubId(), snapshot);

                        ProducerRecord<String, SensorsSnapshotAvro> outRecord = new ProducerRecord<>(
                                "telemetry.snapshots.v1",
                                snapshot.getHubId().toString(),
                                snapshot
                        );

                        producer.send(outRecord, (metadata, ex) -> {
                            if (ex != null) {
                                log.error("Ошибка при отправке снапшота в Kafka", ex);
                            } else {
                                log.debug("Снапшот успешно отправлен: topic={}, partition={}, offset={}",
                                        metadata.topic(), metadata.partition(), metadata.offset());
                            }
                        });
                    } else {
                        log.debug("Снапшот не сформирован для события hubId={}", event.getHubId());
                    }
                }

                log.trace("Отправка асинхронного коммита...");
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при commitAsync", exception);
                    } else {
                        log.trace("Коммит выполнен: {}", offsets);
                    }
                });
            }
        } catch (WakeupException ignored) {
            log.info("WakeupException: получен сигнал завершения");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Завершаем: flush и commitSync");
                producer.flush();
                consumer.commitSync();
            } catch (Exception e) {
                log.error("Ошибка при завершении и commitSync", e);
            } finally {
                log.info("Закрываем продюсер и консьюмер");
                try {
                    producer.close();
                } catch (Exception e) {
                    log.error("Ошибка при закрытии продюсера", e);
                }
                try {
                    consumer.close();
                } catch (Exception e) {
                    log.error("Ошибка при закрытии консьюмера", e);
                }
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
