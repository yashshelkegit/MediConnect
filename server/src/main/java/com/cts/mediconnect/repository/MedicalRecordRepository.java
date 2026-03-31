package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    List<MedicalRecord> findByPatient_PatientId(Integer patientId);
    List<MedicalRecord> findByDoctor_DoctorId(Integer doctorId);
    List<MedicalRecord> findByHospital_HospitalId(Integer hospitalId);
}
