package com.cts.mediconnect.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Integer hospitalId;

    @Column(name = "hospital_name")
    private String hospitalName;

    private String address;
    private String city;
    private String phone;

    @Column(name = "total_beds")
    private Integer totalBeds;

    @Column(name = "available_beds")
    private Integer availableBeds;

}
