package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.DimensionDto;
import ru.yandex.practicum.model.Dimension;

public class DimensionMapper {

    public static Dimension mapToDimension(DimensionDto dto) {
        return Dimension.builder()
                .depth(dto.getDepth())
                .height(dto.getHeight())
                .width(dto.getWidth())
                .build();
    }
}
