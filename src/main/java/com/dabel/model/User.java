package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String firstName;
    private String lastName;
    private String password;
    private String status;
    private String initiatedBy;
    private String updatedBy;
    private int failedLoginAttempts;
    private LocalDateTime loginAt;

    @Transient
    private List<LoginLog> loginLogs;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @CurrentTimestamp(event = EventType.UPDATE)
    private LocalDateTime updatedAt;

    @Transient
    private String role;
}
