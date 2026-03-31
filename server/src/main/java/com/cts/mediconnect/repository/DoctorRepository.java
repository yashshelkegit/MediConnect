package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByUser_UserId(Integer userId);
    List<Doctor> findByHospital_HospitalId(Integer hospitalId);
    List<Doctor> findByDepartment_DepartmentId(Integer departmentId);
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findByAvailabilityStatus(String availabilityStatus);
}
