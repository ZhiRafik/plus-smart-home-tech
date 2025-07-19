package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.handler.HubEventHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, byte[]> consumer;
    private final List<HubEventHandler> handlers;

    @Override
    public void run() {
        log.info("Запуск HubEventProcessor...");

        Map<HubEventProto.PayloadCase, HubEventHandler> handlerMap = new HashMap<>();
        for (HubEventHandler handler : handlers) {
            handlerMap.put(handler.getMessageType(), handler);
        }

        SpecificDatumReader<HubEventProto> reader = new SpecificDatumReader<>(HubEventProto.class);

        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records) {
                byte[] bytes = record.value();

                try {
                    BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
                    HubEventProto event = reader.read(null, decoder);
                    HubEventProto.PayloadCase type = event.getPayloadCase();

                    HubEventHandler handler = handlerMap.get(type);
                    if (handler != null) {
                        handler.handle(event);
                    } else {
                        log.warn("Нет обработчика для события типа {}", type);
                    }

                } catch (Exception e) {
                    log.error("Ошибка при десериализации или обработке события: {}", e.getMessage(), e);
                }
            }
        }
    }

}