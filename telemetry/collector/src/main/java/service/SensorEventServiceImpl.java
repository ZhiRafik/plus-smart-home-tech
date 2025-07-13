package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.SensorEventMapper;
import model.sensor.SensorEvent;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventServiceImpl implements SensorEventService {

    private static final String TOPIC = "telemetry.sensors.v1";

    private final Producer<String, SpecificRecordBase> producer;

    @Override
    public void collect(SensorEvent sensorEvent) {
        SensorEventAvro avroEvent = SensorEventMapper.mapToAvro(sensorEvent);

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                TOPIC,
                avroEvent.getId(),
                avroEvent
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке sensor-события в Kafka: {}", exception.getMessage(), exception);
            } else {
                log.info("Sensor-событие отправлено в Kafka: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}

