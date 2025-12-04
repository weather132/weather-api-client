package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.TemperatureEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemperatureRepository extends CrudRepository<TemperatureEntity, String> {
}
