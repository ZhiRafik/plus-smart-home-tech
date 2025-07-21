package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.handler.HubEventHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    @Autowired
    private KafkaConsumer<String, SpecificRecordBase> consumer;
    @Autowired
    private List<HubEventHandler> handlers;
    private final String topic = "telemetry.hubs.v1";

    public HubEventProcessor(List<HubEventHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void run() {
        log.info("Запуск HubEventProcessor с Avro...");

        try {
            consumer.subscribe(List.of(topic));
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    if (!(record.value() instanceof HubEventAvro event)) {
                        log.warn("Неожиданный тип сообщения: {}", record.value().getClass().getSimpleName());
                        continue;
                    }

                    Object payload = event.getPayload();

                    boolean handled = false;
                    for (HubEventHandler handler : handlers) {
                        if (handler.supports(payload)) {
                            log.debug("Обработка события {} для хаба {}", payload.getClass().getSimpleName(), event.getHubId());
                            handler.handle(event);
                            handled = true;
                            break;
                        }
                    }

                    if (!handled) {
                        log.warn("Нет обработчика для события: {}", payload.getClass().getSimpleName());
                    }
                }

                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при commitAsync", exception);
                    } else {
                        log.trace("Коммит смещений: {}", offsets);
                    }
                });
            }
        } catch (WakeupException e) {
            log.info("Получен сигнал wakeup, завершаем HubEventProcessor...");
        } catch (Exception e) {
            log.error("Неожиданная ошибка в HubEventProcessor", e);
        } finally {
            consumer.close();
            log.info("Kafka consumer закрыт");
        }
    }


    @PreDestroy
    public void shutdown() {
        log.info("Завершение HubEventProcessor...");
        consumer.wakeup();
    }


}