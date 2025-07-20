package ru.yandex.practicum.telemetry.analyzer.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtoUtils {

    @SuppressWarnings("unchecked")
    public <T extends Message> T deserialize(byte[] data, Parser<T> parser) {
        try {
            return parser.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Ошибка при десериализации Proto: ", e);
        }
    }
}
