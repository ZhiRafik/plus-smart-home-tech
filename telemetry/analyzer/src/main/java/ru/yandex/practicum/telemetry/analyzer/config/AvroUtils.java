package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.experimental.UtilityClass;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.IOException;

@UtilityClass
public class AvroUtils {

    @SuppressWarnings("unchecked")
    public <T extends SpecificRecordBase> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        DatumReader<T> reader = new SpecificDatumReader<>(clazz);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }
}
