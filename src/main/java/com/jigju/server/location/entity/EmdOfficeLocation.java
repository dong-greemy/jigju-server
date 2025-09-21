package com.jigju.server.location.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name="emd_office_location")
@Getter
@Immutable
public class EmdOfficeLocation {

    @Id
    @Column(nullable = false)
    private int id;

    @Column
    private String province;

    @Column
    private String district;

    @Column
    private String emd_office;

    @Column
    private int postal_code;

    @Column
    private String address;

    protected EmdOfficeLocation() {}

}
