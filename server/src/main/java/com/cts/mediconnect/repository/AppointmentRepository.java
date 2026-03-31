package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPatient_PatientId(Integer patientId);
    List<Appointment> findByDoctor_DoctorId(Integer doctorId);
    List<Appointment> findByHospital_HospitalId(Integer hospitalId);
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDate(Integer doctorId, LocalDate date);
    List<Appointment> findByStatus(String status);
}
