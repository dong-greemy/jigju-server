package com.jigju.server.location.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "haengjeong_emd_code")
@Getter
@Immutable
public class HaengjeongEmdCode {
    @Id
    @Column(nullable = false, name="administrative_code")
    private int administrativeCode;

    @Column
    private String province;

    @Column
    private String district;

    @Column
    private String emd;

    protected HaengjeongEmdCode() {}
}
