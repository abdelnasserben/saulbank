package com.dabel.dto;

import com.dabel.app.validation.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserDto  extends BasicDto {

    private Long userId;
    @NotBlank
    private String username;
    private String password;
    @UserRole
    private String role;
    private int failedLoginAttempts;
    private LocalDateTime loginAt;
}
