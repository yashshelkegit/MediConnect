package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Integer> {
    List<Bed> findByHospital_HospitalId(Integer hospitalId);
    List<Bed> findByHospital_HospitalIdAndStatus(Integer hospitalId, String status);
    List<Bed> findByPatient_PatientId(Integer patientId);
}
