package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.HubEventMapper;
import model.hub.HubEvent;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService {

    private static final String TOPIC = "telemetry.hubs.v1";

    private final Producer<String, SpecificRecordBase> producer;

    @Override
    public void collect(HubEvent hubEvent) {
        HubEventAvro avroEvent = HubEventMapper.mapToAvro(hubEvent);

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                TOPIC,
                avroEvent.getHubId(),
                avroEvent
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке события в Kafka: {}", exception.getMessage(), exception);
            } else {
                log.info("Событие успешно отправлено в Kafka. Topic: {}, Partition: {}, Offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}