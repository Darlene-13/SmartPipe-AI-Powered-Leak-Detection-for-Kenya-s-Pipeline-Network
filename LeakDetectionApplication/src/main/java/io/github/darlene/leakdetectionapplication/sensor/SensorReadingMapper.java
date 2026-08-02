package io.github.darlene.leakdetectionapplication.sensor;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import io.github.darlene.leakdetectionapplication.sensor.SensorReading;
import io.github.darlene.leakdetectionapplication.sensor.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.sensor.SensorReadingResponse;

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