package com.example.tempfit.repository;

import com.example.tempfit.entity.Coordi;
import com.example.tempfit.entity.TemperatureRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoordiRepository extends JpaRepository<Coordi, Long> {

    List<Coordi> findTop3ByCommunityStyleCasualTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop3ByCommunityStyleStreetTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop3ByCommunityStyleFormalTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findTop3ByCommunityStyleOutdoorTrueAndTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);

    List<Coordi> findByTemperatureRangeOrderByRecommendCountDesc(
        TemperatureRange temperatureRange);
}
