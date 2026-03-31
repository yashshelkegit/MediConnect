package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    List<Inventory> findByHospital_HospitalId(Integer hospitalId);
    List<Inventory> findByHospital_HospitalIdAndCategory(Integer hospitalId, String category);
}
