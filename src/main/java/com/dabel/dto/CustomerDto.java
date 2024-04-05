package com.dabel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomerDto extends BasicDto {

    private Long customerId;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String gender;
    private String identityNumber;
    private String identityType;
    private String identityIssue;
    private LocalDate identityExpiration;
    private LocalDate birthDate;
    private String birthPlace;
    private String nationality;
    private String residence;
    private String address;
    private String postCode;
    private String phone;
    private String email;
    private String profession;
    private String profilePicture;
    private String signaturePicture;
    private String identityPicture;
}
