package com.example.tempfit.repository;

import com.example.tempfit.entity.Coordi;
import com.example.tempfit.entity.TemperatureRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoordiRepository extends JpaRepository<Coordi, Long> {

    List<Coordi> findTop5ByCommunityStyleCasualTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop5ByCommunityStyleStreetTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop5ByCommunityStyleFormalTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop5ByCommunityStyleOutdoorTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findByTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);
}
