package com.cts.mediconnect.service;

import com.cts.mediconnect.model.Hospital;
import com.cts.mediconnect.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Optional<Hospital> getHospitalById(Integer id) {
        return hospitalRepository.findById(id);
    }

    public List<Hospital> getHospitalsByCity(String city) {
        return hospitalRepository.findByCity(city);
    }

    public Hospital createHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    public Optional<Hospital> updateHospital(Integer id, Hospital updatedHospital) {
        return hospitalRepository.findById(id).map(existing -> {
            existing.setHospitalName(updatedHospital.getHospitalName());
            existing.setAddress(updatedHospital.getAddress());
            existing.setCity(updatedHospital.getCity());
            existing.setPhone(updatedHospital.getPhone());
            existing.setTotalBeds(updatedHospital.getTotalBeds());
            existing.setAvailableBeds(updatedHospital.getAvailableBeds());
            return hospitalRepository.save(existing);
        });
    }

    public boolean deleteHospital(Integer id) {
        if (hospitalRepository.existsById(id)) {
            hospitalRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
