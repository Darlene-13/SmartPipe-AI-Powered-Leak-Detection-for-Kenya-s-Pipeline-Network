package io.github.darlene.leakdetectionapplication.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;

@Mapper(componentModel = "spring")
public interface SensorReadingMapper {

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "dpDtA",      ignore = true)
    @Mapping(target = "dpDtB",      ignore = true)
    @Mapping(target = "dpDtC",      ignore = true)
    @Mapping(target = "prediction", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    SensorReading toEntity(SensorReadingRequest request);

    SensorReadingResponse toResponse(SensorReading entity);

    List<SensorReadingResponse> toResponseList(List<SensorReading> entities);
}