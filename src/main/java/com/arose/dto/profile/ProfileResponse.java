package com.arose.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {

    private String id;
    private String name;
    private String email;

    private String profilePhotoUrl;

    private Double height;
    private Double weight;

    private Double bust;
    private Double waist;
    private Double hips;
    private Double shoulder;
    private Double inseam;

    private String unit;
}