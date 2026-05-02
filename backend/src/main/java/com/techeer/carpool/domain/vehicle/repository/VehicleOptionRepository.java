package com.techeer.carpool.domain.vehicle.repository;

import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleOptionRepository extends JpaRepository<VehicleOption, Long> {

    @Query("SELECT DISTINCT v.brand, v.model FROM VehicleOption v ORDER BY v.brand ASC, v.model ASC")
    List<Object[]> findDistinctBrandAndModel();

    List<VehicleOption> findByBrandAndModelOrderByColorAsc(String brand, String model);
}
