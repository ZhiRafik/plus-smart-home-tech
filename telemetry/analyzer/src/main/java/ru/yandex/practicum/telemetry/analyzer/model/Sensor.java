package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.grpc.telemetry.event.enums.DeviceTypeProto;

@Data
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;
}

