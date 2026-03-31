package com.cts.mediconnect.service;

import com.cts.mediconnect.model.Department;
import com.cts.mediconnect.model.Hospital;
import com.cts.mediconnect.repository.DepartmentRepository;
import com.cts.mediconnect.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Integer id) {
        return departmentRepository.findById(id);
    }

    public List<Department> getDepartmentsByHospitalId(Integer hospitalId) {
        return departmentRepository.findByHospital_HospitalId(hospitalId);
    }

    public Optional<Department> createDepartment(Integer hospitalId, Department department) {
        Optional<Hospital> hospital = hospitalRepository.findById(hospitalId);
        if (hospital.isEmpty()) {
            return Optional.empty();
        }
        department.setHospital(hospital.get());
        return Optional.of(departmentRepository.save(department));
    }

    public Optional<Department> updateDepartment(Integer id, Department updatedDepartment) {
        return departmentRepository.findById(id).map(existing -> {
            existing.setDepartmentName(updatedDepartment.getDepartmentName());
            if (updatedDepartment.getHospital() != null && updatedDepartment.getHospital().getHospitalId() != null) {
                hospitalRepository.findById(updatedDepartment.getHospital().getHospitalId())
                        .ifPresent(existing::setHospital);
            }
            return departmentRepository.save(existing);
        });
    }

    public boolean deleteDepartment(Integer id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
