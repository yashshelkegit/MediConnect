package com.cts.mediconnect.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    private String specialization;

    @Column(name = "availability_status")
    private String availabilityStatus;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @Column(name = "blood_group")
    private String bloodGroup;

    private String address;

    @Column(name = "emergency_contact")
    private String emergencyContact;
}
