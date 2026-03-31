package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Integer> {
    List<LabReport> findByPatient_PatientId(Integer patientId);
    List<LabReport> findByDoctor_DoctorId(Integer doctorId);
    List<LabReport> findByHospital_HospitalId(Integer hospitalId);
}
