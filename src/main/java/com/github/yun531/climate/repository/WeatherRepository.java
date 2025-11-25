package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.Weather;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends CrudRepository<Weather, String> {
}
