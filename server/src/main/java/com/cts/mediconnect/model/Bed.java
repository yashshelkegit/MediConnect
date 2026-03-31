package com.cts.mediconnect.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "beds")
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bed_id")
    private Integer bedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    private String ward;

    @Column(name = "bed_number")
    private String bedNumber;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
