package com.arose.dto.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    private String name;

    private String email;

    private Double height;

    private Double weight;

    private Double bust;

    private Double waist;

    private Double hips;

    private Double shoulder;

    private Double inseam;

    private String unit;
}